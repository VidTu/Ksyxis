/*
 * Ksyxis is a third-party mod for Minecraft Java Edition that
 * speed ups your world loading by removing unneeded chunks.
 *
 * MIT License
 *
 * Copyright (c) 2021-2026 VidTu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * SPDX-License-Identifier: MIT
 */

import com.google.gson.Gson
import com.google.gson.JsonElement
import ru.vidtu.ksyxis.buildsrc.Strip

// Plugins.
plugins {
    id("java")
    alias(libs.plugins.blossom)
    alias(libs.plugins.idea.ext)
}

// Language.
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

// Metadata.
group = "ru.vidtu.ksyxis"
base.archivesName = "Ksyxis"
description = "Speed up your world loading by removing unneeded chunks."

// Buildscript dependencies.
buildscript {
    dependencies {
        // Add GSON to buildscript classpath, we use it for minifying JSON files.
        classpath(libs.gson)

        // Override GSON's outdated annotations.
        classpath(libs.error.prone.annotations)
    }
}

// Repositories for dependencies.
repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/") // Mixin.
}

// Dependencies.
dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Minecraft.
    compileOnly(libs.mixin)
    compileOnly(libs.asm) // Required for Mixin.
    compileOnly(libs.log4j) { // Not SLF4J for compatibility with pre-1.18.
        exclude("biz.aQute.bnd")
        exclude("com.github.spotbugs")
        exclude("com.google.errorprone")
        exclude("org.osgi")
    }
}

tasks.withType<JavaCompile> {
    // Compile with UTF-8.
    options.encoding = "UTF-8"

    // Set the compiler debug options.
    if ("${findProperty("ru.vidtu.ksyxis.debug.javac") ?: findProperty("ru.vidtu.ksyxis.debug")}".toBoolean()) {
        options.compilerArgs.addAll(listOf("-g", "-parameters"))
    } else if ("${findProperty("ru.vidtu.ksyxis.slim")}".toBoolean()) {
        options.compilerArgs.add("-g:none")
    } else {
        options.compilerArgs.add("-g")
    }

    // JDK 8 (used by this project) doesn't support the "-release" flag and
    // uses "-source" and "-target" ones (see the top of the file),
    // so we must NOT specify it, or the "javac" will fail.
    // If we ever gonna compile on newer Java versions, uncomment this line.
    // options.release = 8

    // Post-process classes. (strip metadata)
    if (!"${findProperty("ru.vidtu.ksyxis.debug.metadata") ?: findProperty("ru.vidtu.ksyxis.debug")}".toBoolean()) {
        doLast {
            Strip(destinationDirectory.get().asFile, classpath).use { strip ->
                destinationDirectory.asFileTree
                    .filter { it.name != "package-info.class" }
                    .forEach { strip.stripBytecode(it) }
            }
        }
    }
}

sourceSets.main {
    // Add compile-time stub classes.
    java.srcDir("src/main/java-compile")

    // Expand compile-time variables.
    blossom.javaSources {
        val fallbackProvider = providers.gradleProperty("ru.vidtu.ksyxis.debug").orElse("false")
        property("debugAsserts", providers.gradleProperty("ru.vidtu.ksyxis.debug.asserts").orElse(fallbackProvider))
        property("debugLogs", providers.gradleProperty("ru.vidtu.ksyxis.debug.logs").orElse(fallbackProvider))
        property("version", "${version}")
    }
}

tasks.withType<ProcessResources> {
    // Filter with UTF-8.
    filteringCharset = "UTF-8"

    // Expand version.
    inputs.property("version", version)
    filesMatching(listOf("fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml", "mcmod.info")) {
        expand(inputs.properties)
    }

    // Minify JSON-alike (including ".mcmeta" and ".info") and TOML files.
    if (!"${findProperty("ru.vidtu.ksyxis.debug.resources") ?: findProperty("ru.vidtu.ksyxis.debug")}".toBoolean()) {
        var files = fileTree(outputs.files.asPath)
        doLast {
            val jsonAlike = Regex("^.*\\.(?:json|mcmeta|info)$", RegexOption.IGNORE_CASE)
            files.forEach {
                if (it.name.matches(jsonAlike)) {
                    it.writeText(Gson().fromJson(it.readText(), JsonElement::class.java).toString())
                } else if (it.name.endsWith(".toml", ignoreCase = true)) {
                    it.writeText(it.readLines()
                        .filter { !it.startsWith('#') }
                        .filter { it.isNotBlank() }
                        .joinToString("\n")
                        .replace(" = ", "="))
                }
            }
        }
    }
}

tasks.withType<Jar> {
    // Add LICENSE.
    from("LICENSE")

    // Exclude compile-only code.
    exclude("net/**")
    exclude("org/**")
    exclude("ru/vidtu/ksyxis/compile/**")

    // Remove package-info.class, unless package debug is on. (to save space)
    if (!"${findProperty("ru.vidtu.ksyxis.debug.package") ?: findProperty("ru.vidtu.ksyxis.debug")}".toBoolean()) {
        exclude("**/package-info.class")
    }

    // Add manifest.
    manifest {
        attributes(
            "FMLCorePlugin" to "ru.vidtu.ksyxis.platform.KCore", // Forge pre-1.13.
            "FMLCorePluginContainsFMLMod" to "true", // Forge pre-1.13.
            "ForceLoadAsMod" to "true", // Forge pre-1.13.
            "MixinConfigs" to "ksyxis.mixins.json" // Forge and old NeoForge.
        )
    }
}

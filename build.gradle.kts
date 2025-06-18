/*
 * MIT License
 *
 * Copyright (c) 2021-2025 VidTu
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

plugins {
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8
java.toolchain.languageVersion = JavaLanguageVersion.of(8)

group = "ru.vidtu.ksyxis"
base.archivesName = "Ksyxis"
description = "Speed up the loading of your world."

// Add GSON to buildscript classpath, we use it for minifying JSON files.
buildscript {
    dependencies {
        classpath(libs.gson)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/") // Mixin.
}

dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)

    // Minecraft.
    compileOnly(project(":loaders"))
    compileOnly(libs.mixin)
    compileOnly(libs.asm) // Required for Mixin.
    compileOnly(libs.log4j) { // Not SLF4J for compatibility with pre-1.18.
        exclude("biz.aQute.bnd")
        exclude("com.github.spotbugs")
        exclude("com.google.errorprone")
        exclude("org.osgi")
    }
}

// Compile with UTF-8, Java 8, and with all debug options.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    // JDK 8 (used by this buildscript) doesn't support the "-release" flag
    // (at the top of the file), so we must NOT specify it or the "javac" will fail.
    // If we ever gonna compile on newer Java versions, uncomment this line.
    // options.release = 8
}

tasks.withType<ProcessResources> {
    // Expand version.
    inputs.property("version", version)
    filesMatching(listOf("fabric.mod.json", "quilt.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml", "mcmod.info")) {
        expand(inputs.properties)
    }

    // Minify JSON (including ".mcmeta" and ".info") and TOML files.
    var files = fileTree(outputs.files.asPath)
    doLast {
        val jsonAlike = Regex("^.*\\.(?:json|mcmeta|info)$", RegexOption.IGNORE_CASE)
        files.forEach {
            if (it.name.matches(jsonAlike)) {
                it.writeText(Gson().fromJson(it.readText(), JsonElement::class.java).toString())
            } else if (it.name.endsWith(".toml", ignoreCase = true)) {
                it.writeText(it.readLines()
                    .filter { s -> s.isNotBlank() }
                    .joinToString("\n")
                    .replace(" = ", "="))
            }
        }
    }
}

// Reproducible builds.
tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

// Add LICENSE and manifest into the JAR file.
// Manifest also controls Mixin/mod loading on some loaders/versions.
tasks.withType<Jar> {
    from("LICENSE")
    manifest {
        attributes(
            "Specification-Title" to "Ksyxis",
            "Specification-Version" to version,
            "Specification-Vendor" to "VidTu",
            "Implementation-Title" to "Ksyxis",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "VidTu",
            "FMLCorePlugin" to "ru.vidtu.ksyxis.platform.KCore",
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true",
            "MixinConfigs" to "ksyxis.mixins.json"
        )
    }
}

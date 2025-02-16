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
 */

plugins {
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8
java.toolchain.languageVersion = JavaLanguageVersion.of(8)

group = "ru.vidtu.ksyxis"
base.archivesName = "Ksyxis"
description = "Speed up the loading of your world."

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/") // Mixin.
}

dependencies {
    // Annotations
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)

    // Dependencies
    compileOnly(project(":loaders"))
    compileOnly(libs.mixin)
    compileOnly(libs.asm)
    compileOnly(libs.log4j)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
}

tasks.withType<ProcessResources> {
    inputs.property("version", version)
    filesMatching(listOf("fabric.mod.json", "quilt.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml", "mcmod.info")) {
        expand(inputs.properties)
    }
}

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

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
            "FMLCorePlugin" to "ru.vidtu.ksyxis.loaders.KsyxisLegacyForge",
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true",
            "MixinConfigs" to "ksyxis.mixins.json"
        )
    }
}

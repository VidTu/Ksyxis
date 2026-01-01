/*
 * Ksyxis is a third-party mod for Minecraft Java Edition that
 * speed ups your world loading by removing spawn chunks.
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

plugins {
    id("java")
}

// Language.
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

// Metadata.
group = "ru.vidtu.ksyxis.loaders"
base.archivesName = "Ksyxis-Loaders"
description = "Module with stubs of loaders' classes for Ksyxis."

// Compile with UTF-8, Java 8, and with all debug options.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    // JDK 8 (used by this buildscript) doesn't support the "-release" flag
    // (at the top of the file), so we must NOT specify it or the "javac" will fail.
    // If we ever gonna compile on newer Java versions, uncomment this line.
    // options.release = 8
}

// Add LICENSE and manifest into the JAR file.
tasks.withType<Jar> {
    manifest {
        attributes(
            "Specification-Title" to "Ksyxis",
            "Specification-Version" to version,
            "Specification-Vendor" to "VidTu",
            "Implementation-Title" to "Ksyxis-Loaders",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "VidTu, FabricMC, MinecraftForge, NeoForged, QuiltMC"
        )
    }
}

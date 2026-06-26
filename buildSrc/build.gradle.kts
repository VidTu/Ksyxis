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

// Plugins.
plugins {
    id("java")
}

// Language.
java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

// Metadata.
group = "ru.vidtu.ksyxis.build"
base.archivesName = "Ksyxis_buildSrc"
description = "Speed up your world loading by removing unneeded chunks. (buildSrc)"

// Repositories for dependencies.
repositories {
    mavenCentral()
}

// Dependencies.
dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)
}

// Compile with UTF-8, Java 25, and with all debug options.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters", "-Xlint:all"))
    options.release = 25
}

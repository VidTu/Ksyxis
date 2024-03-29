plugins {
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8
java.toolchain.languageVersion = JavaLanguageVersion.of(8)
group = "ru.vidtu.ksyxis.loaders"

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

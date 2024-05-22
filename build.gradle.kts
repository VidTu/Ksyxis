plugins {
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8
java.toolchain.languageVersion = JavaLanguageVersion.of(8)
group = "ru.vidtu.ksyxis"
description = "Speed up the loading of your world."

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    compileOnly(project(":loaders"))
    compileOnly("org.apache.logging.log4j:log4j-api:2.23.1")
    compileOnly("org.spongepowered:mixin:0.8.6")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    filesMatching(
        listOf(
            "fabric.mod.json",
            "quilt.mod.json",
            "META-INF/mods.toml",
            "META-INF/neoforge.mods.toml",
            "mcmod.info"
        )
    ) {
        expand("version" to project.version)
    }
}

tasks.withType<Jar> {
    from("LICENSE")
    manifest {
        attributes(
            "Specification-Title" to "Ksyxis",
            "Specification-Version" to project.version,
            "Specification-Vendor" to "VidTu",
            "Implementation-Title" to "Ksyxis",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "VidTu",
            "FMLCorePlugin" to "ru.vidtu.ksyxis.loaders.KsyxisLegacyForge",
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true",
            "MixinConfigs" to "ksyxis.mixins.json"
        )
    }
}

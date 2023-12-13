plugins {
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8
java.toolchain.languageVersion = JavaLanguageVersion.of(8)
group = "ru.vidtu.ksyxis"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.minecraftforge.net/")
    maven("https://libraries.minecraft.net/")
}

dependencies {
    val log4j = project.properties["log4j"]
    val mixin = project.properties["mixin"]
    compileOnly(project(":loaders"))
    compileOnly("org.apache.logging.log4j:log4j-api:${log4j}")
    compileOnly("org.spongepowered:mixin:${mixin}")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    filesMatching(listOf("fabric.mod.json", "quilt.mod.json", "META-INF/mods.toml", "mcmod.info")) {
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
                "FMLCorePlugin" to "ru.vidtu.ksyxis.KsyxisLegacyForge",
                "FMLCorePluginContainsFMLMod" to "true",
                "ForceLoadAsMod" to "true",
                "MixinConfigs" to "ksyxis.mixins.json"
        )
    }
}

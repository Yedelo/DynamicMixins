plugins {
    id("java")
    kotlin("jvm")
    id("net.kyori.blossom") version "1.3.1"
}

group = "at.yedel"
version = properties["version"]!!

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public")
    maven("https://libraries.minecraft.net")
}

dependencies {
    implementation("org.spongepowered:mixin:0.8.4") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    implementation("net.minecraft:launchwrapper:1.12")
    implementation("com.google.code.gson:gson:2.2.4")
}

blossom {
    replaceTokenIn("src/main/java/at/yedel/dynamicmixins/DynamicMixins.java")
    replaceToken("#version#", version)
}

tasks {
    jar {
        archiveVersion.set("$version")
    }
}
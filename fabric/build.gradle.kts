buildscript {
    repositories {
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("java")
    id("fabric-loom") version "1.7-SNAPSHOT"
}

repositories {
    maven("https://libraries.minecraft.net")
    maven("https://maven.fabricmc.net/")
}

val minecraft_version = "1.20.1"
val yarn_mappings = "1.20.1+build.6"
val loader_version = "0.14.21"
val fabric_version = "0.86.0+1.20.1"

dependencies {
    implementation(project(":common"))
    implementation(project(":brigadier"))
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.fabricmc:yarn:${yarn_mappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loader_version}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

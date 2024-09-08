import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("java")
    kotlin("jvm") version "2.0.10"
    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("com.gradleup.shadow") version "8.3.0"

    // Adds runServer and runMojangMappedServer tasks for testing
    id("xyz.jpenilla.run-paper") version "2.2.4"

    // Generates plugin.yml based on the Gradle config
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
}

group = "io.github.revxrsal"
version = "4.0.0-beta.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":brigadier"))
    implementation(project(":bukkit"))
    implementation(project(":paper"))
    implementation(kotlin("stdlib-jdk8"))
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

// Generate our plugin.yml
bukkitPluginYaml {
    main = "com.example.plugin.TestPlugin"
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors.add("Revxrsal")
    apiVersion = "1.20.5"
}

tasks["build"].dependsOn(tasks.shadowJar)
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("java")
    kotlin("jvm") version "2.0.10"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://hub.spigotmc.org/nexus/content/groups/public/")
    maven(url = "https://libraries.minecraft.net")
    maven(url = "https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":brigadier"))
    implementation(project(":bukkit"))
    implementation(project(":paper"))
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
}

tasks.getByName<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}

tasks.withType<JavaCompile> {
    // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

tasks.withType<KotlinJvmCompile> { // optional: if you're using Kotlin
    compilerOptions {
        javaParameters = true
    }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
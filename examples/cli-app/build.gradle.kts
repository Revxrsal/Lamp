plugins {
    id("java")
    kotlin("jvm")
}

group = "io.github.revxrsal"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":cli"))
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<JavaCompile> {
    // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

kotlin {
    jvmToolchain(17)
}
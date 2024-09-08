plugins {
    id("java")
}

group = "io.github.revxrsal"
version = "4.0.0-beta.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":minestom"))
    implementation("net.minestom:minestom-snapshots:7ce047b22e")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

tasks.withType<JavaCompile> {
    // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}
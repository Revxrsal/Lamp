plugins {
    id("java")
}

group = "io.github.revxrsal"
version = "4.0.0-beta.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":minestom"))
    implementation("net.minestom:minestom-snapshots:1_20_5-dd965f4bb8")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

tasks.withType<JavaCompile> {
    // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}
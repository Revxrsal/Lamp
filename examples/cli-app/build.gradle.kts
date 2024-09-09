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
    implementation(project(":cli"))
}

tasks.withType<JavaCompile> {
    // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

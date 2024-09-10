plugins {
    id("java")
}

group = "io.github.revxrsal"

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

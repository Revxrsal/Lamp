plugins {
    id("java")
}

group = "io.github.revxrsal"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":jda"))
    implementation("net.dv8tion:JDA:5.1.0")
}

tasks.withType<JavaCompile> {
    // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

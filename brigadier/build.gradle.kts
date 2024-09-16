plugins {
    id("java")
}

group = "io.github.revxrsal"

repositories {
    mavenCentral()
    maven(url = "https://libraries.minecraft.net")
}

dependencies {
    implementation(project(":common"))
    implementation("com.mojang:brigadier:1.0.18")
}


tasks.withType<JavaCompile> {
    // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

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
    compileOnly("com.mojang:brigadier:1.0.18")
}

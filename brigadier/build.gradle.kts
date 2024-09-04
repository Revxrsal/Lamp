plugins {
    id("java")
}

group = "io.github.revxrsal"
version = "4.0.0-beta.2"

repositories {
    mavenCentral()
    maven(url = "https://libraries.minecraft.net")
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.mojang:brigadier:1.0.18")
}

plugins {
    id("java")
}

group = "io.github.revxrsal"
version = "4.0.0-beta.3"

repositories {
    mavenCentral()
    maven(url = "https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    implementation(project(":common"))
    compileOnly("org.spongepowered:spongeapi:8.0.0")
}

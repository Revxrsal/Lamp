plugins {
    id("java")
}

group = "io.github.revxrsal"

repositories {
    mavenCentral()
    maven(url = "https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    implementation(project(":common"))
    compileOnly("org.spongepowered:spongeapi:8.0.0")
}

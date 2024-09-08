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
    compileOnly("net.minestom:minestom-snapshots:7ce047b22e")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
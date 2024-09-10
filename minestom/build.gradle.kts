plugins {
    id("java")
}

group = "io.github.revxrsal"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    compileOnly("net.minestom:minestom-snapshots:7ce047b22e")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
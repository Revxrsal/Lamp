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
    testImplementation("com.mojang:brigadier:1.0.18")
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

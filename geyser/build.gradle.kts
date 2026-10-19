plugins {
    id("java")
}

val javaVersion = 21

repositories {
    maven(url = "https://repo.opencollab.dev/main/")
}

dependencies {
    implementation(project(":common"))
    compileOnly("org.geysermc.geyser:api:2.11.2-SNAPSHOT")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))

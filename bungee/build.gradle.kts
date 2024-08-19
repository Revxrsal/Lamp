plugins {
    id("java")
}

repositories {
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://hub.spigotmc.org/nexus/content/groups/public/")
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("net.md-5:bungeecord-api:1.16-R0.4")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
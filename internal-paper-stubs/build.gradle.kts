plugins {
    id("java")
}

repositories {
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://hub.spigotmc.org/nexus/content/groups/public/")
    maven(url = "https://libraries.minecraft.net")
    maven(url = "https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("net.kyori:adventure-api:4.14.0")
}

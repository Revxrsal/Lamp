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
    implementation(project(":common"))
    implementation(project(":brigadier"))
    compileOnly(project(":internal-paper-stubs")) {
        exclude(module = "spigot-api")
        exclude(module = "brigadier")
    }
    compileOnly("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT") {
        exclude(module = "spigot-api")
        exclude(module = "adventure")
    }
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.mojang:brigadier:1.0.18")

    compileOnly("net.kyori:adventure-platform-bukkit:4.3.4")
}

pluginManagement {
    plugins {
        kotlin("jvm") version "2.0.10"
    }
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "lamp"

include("common")
include("bukkit")
include("paper")
include("bungee")
include("brigadier")
include("velocity")
include("cli")
include("sponge")
include("jda")
include("fabric")
include("minestom")
include("internal-paper-stubs")

/*
 * -------- Example projects --------
 */

include("examples")

val exampleProjects = listOf(
    "bukkit-plugin",
    "jda-bot",
    "minestom-server",
    "cli-app"
)

exampleProjects.forEach { project ->
    include("examples:$project")
    findProject(":examples:$project")?.name = project
}

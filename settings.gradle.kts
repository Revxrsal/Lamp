pluginManagement {
    plugins {
        kotlin("jvm") version "2.0.10"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
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

/* example projects */
include("examples")

include("examples:bukkit-plugin")
findProject(":examples:bukkit-plugin")?.name = "bukkit-plugin"

include("examples:jda-bot")
findProject(":examples:jda-bot")?.name = "jda-bot"

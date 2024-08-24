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
include("sample-plugin")
include("velocity")
include("sponge")
include("jda")

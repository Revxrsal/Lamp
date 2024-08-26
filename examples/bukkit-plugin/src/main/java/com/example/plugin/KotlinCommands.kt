package com.example.plugin

import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.actor.BukkitCommandActor

class KotlinCommands {

    @Command("teleport")
    fun teleport(
        actor: BukkitCommandActor,
        x: Int,
        y: Int,
        @Optional z: Int = 10
    ) {
        actor.reply("X: $x")
        actor.reply("Y: $y")
        actor.reply("Z: $z")
    }
}

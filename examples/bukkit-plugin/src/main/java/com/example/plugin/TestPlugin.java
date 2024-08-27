package com.example.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitLamp;

public final class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        var lamp = BukkitLamp.builder(this).build();
        lamp.register(new GreetCommands());
    }
}
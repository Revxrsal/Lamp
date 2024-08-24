package com.example.plugin;

import revxrsal.commands.annotation.Command;

public class TeleportCommands {

    @Command("teleport")
    public void teleport(int x, int y, int z) {
        System.out.println(x);
        System.out.println(y);
        System.out.println(z);
    }
}

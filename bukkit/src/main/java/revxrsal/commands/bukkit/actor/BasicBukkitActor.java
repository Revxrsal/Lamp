package revxrsal.commands.bukkit.actor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

record BasicBukkitActor(CommandSender sender, Lamp<BukkitCommandActor> lamp) implements BukkitCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    @Override public @NotNull CommandSender sender() {
        return sender;
    }

    @Override public @NotNull UUID uniqueId() {
        if (isPlayer())
            return ((Player) sender).getUniqueId();
        else if (isConsole())
            return CONSOLE_UUID;
        else
            return UUID.nameUUIDFromBytes(name().getBytes(StandardCharsets.UTF_8));
    }

    @Override public Lamp<BukkitCommandActor> lamp() {
        return lamp;
    }
}
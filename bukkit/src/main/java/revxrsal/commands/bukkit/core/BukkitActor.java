package revxrsal.commands.bukkit.core;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.SenderNotConsoleException;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;

import java.util.UUID;

final class BukkitActor implements BukkitCommandActor {

    private final CommandSender sender;

    public BukkitActor(CommandSender sender) {
        this.sender = sender;
    }

    @Override public CommandSender getSender() {
        return sender;
    }

    @Override public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override public boolean isConsole() {
        return sender instanceof ConsoleCommandSender;
    }

    @Override public @Nullable Player getAsPlayer() {
        return isPlayer() ? (Player) sender : null;
    }

    @Override public @NotNull Player requirePlayer() {
        if (!isPlayer())
            throw new SenderNotPlayerException(this);
        return (Player) sender;
    }

    @Override public @NotNull ConsoleCommandSender requireConsole() {
        if (!isConsole())
            throw new SenderNotConsoleException(this);
        return (ConsoleCommandSender) sender;
    }

    @Override public @NotNull String getName() {
        return sender.getName();
    }

    @Override public @NotNull UUID getUniqueId() {
        return requirePlayer().getUniqueId();
    }

    @Override public void reply(@NotNull String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        sender.sendMessage(message);
    }

    @Override public void error(@NotNull String message) {
        message = ChatColor.translateAlternateColorCodes('&', "&c" + message);
        sender.sendMessage(message);
    }
}

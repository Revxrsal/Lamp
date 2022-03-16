package revxrsal.commands.bukkit.core;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.SenderNotConsoleException;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static revxrsal.commands.util.Preconditions.notNull;
import static revxrsal.commands.util.Strings.colorize;

public final class BukkitActor implements BukkitCommandActor {

    private static final UUID CONSOLE_UUID = UUID.nameUUIDFromBytes("CONSOLE".getBytes(StandardCharsets.UTF_8));

    private final CommandSender sender;
    private final CommandHandler handler;

    public BukkitActor(CommandSender sender, CommandHandler handler) {
        this.sender = notNull(sender, "sender");
        this.handler = notNull(handler, "handler");
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
            throw new SenderNotPlayerException();
        return (Player) sender;
    }

    @Override public @NotNull ConsoleCommandSender requireConsole() {
        if (!isConsole())
            throw new SenderNotConsoleException();
        return (ConsoleCommandSender) sender;
    }

    @Override public @NotNull String getName() {
        return sender.getName();
    }

    @Override public @NotNull UUID getUniqueId() {
        if (isPlayer())
            return ((Player) sender).getUniqueId();
        else if (isConsole())
            return CONSOLE_UUID;
        else
            return UUID.nameUUIDFromBytes(getName().getBytes(StandardCharsets.UTF_8));
    }

    @Override public void reply(@NotNull String message) {
        notNull(message, "message");
        sender.sendMessage(colorize(handler.getMessagePrefix() + message));
    }

    @Override public void error(@NotNull String message) {
        notNull(message, "message");
        sender.sendMessage(colorize(handler.getMessagePrefix() + "&c" + message));
    }

    @Override public CommandHandler getCommandHandler() {
        return handler;
    }
}

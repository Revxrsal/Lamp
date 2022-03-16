package revxrsal.commands.bungee.core;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.bungee.exception.SenderNotPlayerException;

import java.util.Locale;
import java.util.UUID;

import static revxrsal.commands.util.Strings.colorize;

public final class BungeeActor implements BungeeCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private final CommandSender sender;
    private final CommandHandler handler;

    public BungeeActor(CommandSender sender, CommandHandler handler) {
        this.sender = sender;
        this.handler = handler;
    }

    @Override public CommandSender getSender() {
        return sender;
    }

    @Override public boolean isPlayer() {
        return sender instanceof ProxiedPlayer;
    }

    @Override public @Nullable ProxiedPlayer asPlayer() {
        return isPlayer() ? (ProxiedPlayer) sender : null;
    }

    @Override public @NotNull ProxiedPlayer requirePlayer() throws SenderNotPlayerException {
        if (!isPlayer())
            throw new SenderNotPlayerException();
        return (ProxiedPlayer) sender;
    }

    @Override public @NotNull String getName() {
        return sender.getName();
    }

    @Override public @NotNull UUID getUniqueId() {
        return isPlayer() ? ((ProxiedPlayer) sender).getUniqueId() : CONSOLE_UUID;
    }

    @Override public void reply(@NotNull String message) {
        sender.sendMessage(new TextComponent(colorize(handler.getMessagePrefix() + message)));
    }

    @Override public void error(@NotNull String message) {
        reply("&c" + message);
    }

    @Override public CommandHandler getCommandHandler() {
        return handler;
    }

    @Override public @NotNull Locale getLocale() {
        if (isPlayer())
            return requirePlayer().getLocale();
        return BungeeCommandActor.super.getLocale();
    }
}

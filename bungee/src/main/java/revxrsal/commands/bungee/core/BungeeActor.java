package revxrsal.commands.bungee.core;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.bungee.exception.SenderNotPlayerException;

import java.util.UUID;

final class BungeeActor implements BungeeCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private final CommandSender sender;

    public BungeeActor(CommandSender sender) {
        this.sender = sender;
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
            throw new SenderNotPlayerException(this);
        return (ProxiedPlayer) sender;
    }

    @Override public @NotNull String getName() {
        return sender.getName();
    }

    @Override public @NotNull UUID getUniqueId() {
        return isPlayer() ? ((ProxiedPlayer) sender).getUniqueId() : CONSOLE_UUID;
    }

    @Override public void reply(@NotNull String message) {
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
    }

    @Override public void error(@NotNull String message) {
        reply("&c" + message);
    }
}

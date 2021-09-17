package revxrsal.commands.velocity.core;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.velocity.VelocityCommandActor;
import revxrsal.commands.velocity.exception.SenderNotConsoleException;
import revxrsal.commands.velocity.exception.SenderNotPlayerException;

import java.util.UUID;

final class VelocityActor implements VelocityCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private final CommandSource source;
    private final ProxyServer server;

    public VelocityActor(CommandSource source, ProxyServer server) {
        this.source = source;
        this.server = server;
    }

    @Override public @NotNull String getName() {
        return isConsole() ? "Console" : requirePlayer().getUsername();
    }

    @Override public @NotNull UUID getUniqueId() {
        return isConsole() ? CONSOLE_UUID : requirePlayer().getUniqueId();
    }

    @Override public void reply(@NotNull String message) {
        source.sendMessage(Component.text(message.replace('&', '\u00A7')));
    }

    @Override public void error(@NotNull String message) {
        reply("&c" + message);
    }

    @Override public @NotNull CommandSource getSource() {
        return source;
    }

    @Override public @NotNull ProxyServer getServer() {
        return server;
    }

    @Override public boolean isPlayer() {
        return source instanceof Player;
    }

    @Override public boolean isConsole() {
        return source instanceof ConsoleCommandSource;
    }

    @Override public @Nullable Player getAsPlayer() {
        return isPlayer() ? (Player) source : null;
    }

    @Override public @NotNull Player requirePlayer() throws SenderNotPlayerException {
        if (!isPlayer())
            throw new SenderNotPlayerException(this);
        return (Player) source;
    }

    @Override public @NotNull ConsoleCommandSource requireConsole() throws SenderNotConsoleException {
        if (!isPlayer())
            throw new SenderNotConsoleException(this);
        return (ConsoleCommandSource) source;
    }
}

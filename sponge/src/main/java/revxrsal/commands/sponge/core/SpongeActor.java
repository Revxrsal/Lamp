package revxrsal.commands.sponge.core;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.sponge.SpongeCommandActor;
import revxrsal.commands.sponge.exception.SenderNotConsoleException;
import revxrsal.commands.sponge.exception.SenderNotPlayerException;

import java.util.Locale;
import java.util.UUID;

import static revxrsal.commands.util.Strings.colorize;

public final class SpongeActor implements SpongeCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private final CommandCause source;
    private final CommandHandler handler;

    public SpongeActor(CommandCause source, CommandHandler handler) {
        this.source = source;
        this.handler = handler;
    }

    @Override public @NotNull String getName() {
        return isConsole() ? "Console" : requirePlayer().name();
    }

    @Override public @NotNull UUID getUniqueId() {
        return isConsole() ? CONSOLE_UUID : requirePlayer().uniqueId();
    }

    @Override public void reply(@NotNull String message) {
        source.audience().sendMessage(Component.text(colorize(handler.getMessagePrefix() + message)));
    }

    @Override public void error(@NotNull String message) {
        reply("&c" + message);
    }

    @Override public CommandHandler getCommandHandler() {
        return handler;
    }

    @Override public @NotNull CommandCause getSource() {
        return source;
    }

    @Override public boolean isPlayer() {
        return source.subject() instanceof Player;
    }

    @Override public boolean isConsole() {
        return source.subject() instanceof SystemSubject;
    }

    @Override public @Nullable ServerPlayer getAsPlayer() {
        return isPlayer() ? (ServerPlayer) source.subject() : null;
    }

    @Override public @NotNull ServerPlayer requirePlayer() throws SenderNotPlayerException {
        if (!isPlayer())
            throw new SenderNotPlayerException(this);
        return (ServerPlayer) source.subject();
    }

    @Override public @NotNull SystemSubject requireConsole() throws SenderNotConsoleException {
        if (!isPlayer())
            throw new SenderNotConsoleException(this);
        return (SystemSubject) source.subject();
    }

    @Override public @NotNull Locale getLocale() {
        if (isPlayer())
            return requirePlayer().locale();
        return SpongeCommandActor.super.getLocale();
    }

    @Override public void reply(@NotNull Component message) {
        source.audience().sendMessage(message);
    }
}

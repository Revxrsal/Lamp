package revxrsal.commands.sponge.actor;

import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

record BasicSpongeActor(
        CommandCause sender,
        Lamp<SpongeCommandActor> lamp,
        MessageSender<SpongeCommandActor, net.kyori.adventure.text.ComponentLike> messageSender,
        MessageSender<SpongeCommandActor, ComponentLike> errorSender
) implements SpongeCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    @Override public @NotNull CommandCause cause() {
        return sender;
    }

    @Override public void reply(@NotNull ComponentLike message) {
        messageSender.send(this, message);
    }

    @Override public void error(@NotNull ComponentLike message) {
        errorSender.send(this, message);
    }

    @Override public @NotNull UUID uniqueId() {
        if (isPlayer())
            return ((ServerPlayer) sender).uniqueId();
        else if (isConsole())
            return CONSOLE_UUID;
        else
            return UUID.nameUUIDFromBytes(name().getBytes(StandardCharsets.UTF_8));
    }

    @Override public Lamp<SpongeCommandActor> lamp() {
        return lamp;
    }
}
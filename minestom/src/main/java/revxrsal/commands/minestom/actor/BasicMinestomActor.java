package revxrsal.commands.minestom.actor;

import net.kyori.adventure.text.ComponentLike;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

record BasicMinestomActor(
        CommandSender sender,
        Lamp<MinestomCommandActor> lamp,
        MessageSender<MinestomCommandActor, ComponentLike> messageSender,
        MessageSender<MinestomCommandActor, ComponentLike> errorSender
) implements MinestomCommandActor {

    @Override public @NotNull CommandSender sender() {
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
            return ((Player) sender).getUuid();
        else
            return UUID.nameUUIDFromBytes(name().getBytes(StandardCharsets.UTF_8));
    }

    @Override public Lamp<MinestomCommandActor> lamp() {
        return lamp;
    }
}
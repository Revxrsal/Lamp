package revxrsal.commands.velocity.actor;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

record BasicVelocityActor(
        CommandSource sender,
        Lamp<VelocityCommandActor> lamp,
        MessageSender<VelocityCommandActor, ComponentLike> messageSender,
        MessageSender<VelocityCommandActor, ComponentLike> errorSender
) implements VelocityCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    @Override public @NotNull CommandSource source() {
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
            return ((Player) sender).getUniqueId();
        else if (isConsole())
            return CONSOLE_UUID;
        else
            return UUID.nameUUIDFromBytes(name().getBytes(StandardCharsets.UTF_8));
    }

    @Override public Lamp<VelocityCommandActor> lamp() {
        return lamp;
    }
}
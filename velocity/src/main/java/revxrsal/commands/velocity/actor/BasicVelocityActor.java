package revxrsal.commands.velocity.actor;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

final class BasicVelocityActor implements VelocityCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private final CommandSource sender;
    private final Lamp<VelocityCommandActor> lamp;
    private final MessageSender<VelocityCommandActor, ComponentLike> messageSender;
    private final MessageSender<VelocityCommandActor, ComponentLike> errorSender;

    BasicVelocityActor(
            CommandSource sender,
            Lamp<VelocityCommandActor> lamp,
            MessageSender<VelocityCommandActor, ComponentLike> messageSender,
            MessageSender<VelocityCommandActor, ComponentLike> errorSender
    ) {
        this.sender = sender;
        this.lamp = lamp;
        this.messageSender = messageSender;
        this.errorSender = errorSender;
    }

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

    public CommandSource sender() {return sender;}

    public MessageSender<VelocityCommandActor, ComponentLike> messageSender() {return messageSender;}

    public MessageSender<VelocityCommandActor, ComponentLike> errorSender() {return errorSender;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BasicVelocityActor that = (BasicVelocityActor) obj;
        return Objects.equals(this.sender, that.sender) &&
                Objects.equals(this.lamp, that.lamp) &&
                Objects.equals(this.messageSender, that.messageSender) &&
                Objects.equals(this.errorSender, that.errorSender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, lamp, messageSender, errorSender);
    }

    @Override
    public String toString() {
        return "BasicVelocityActor[" +
                "sender=" + sender + ", " +
                "lamp=" + lamp + ", " +
                "messageSender=" + messageSender + ", " +
                "errorSender=" + errorSender + ']';
    }

}
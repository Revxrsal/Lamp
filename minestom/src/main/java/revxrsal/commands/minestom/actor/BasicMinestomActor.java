package revxrsal.commands.minestom.actor;

import net.kyori.adventure.text.ComponentLike;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

final class BasicMinestomActor implements MinestomCommandActor {
    private final CommandSender sender;
    private final Lamp<MinestomCommandActor> lamp;
    private final MessageSender<MinestomCommandActor, ComponentLike> messageSender;
    private final MessageSender<MinestomCommandActor, ComponentLike> errorSender;

    BasicMinestomActor(
            CommandSender sender,
            Lamp<MinestomCommandActor> lamp,
            MessageSender<MinestomCommandActor, ComponentLike> messageSender,
            MessageSender<MinestomCommandActor, ComponentLike> errorSender
    ) {
        this.sender = sender;
        this.lamp = lamp;
        this.messageSender = messageSender;
        this.errorSender = errorSender;
    }

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

    public MessageSender<MinestomCommandActor, ComponentLike> messageSender() {return messageSender;}

    public MessageSender<MinestomCommandActor, ComponentLike> errorSender() {return errorSender;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BasicMinestomActor) obj;
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
        return "BasicMinestomActor[" +
                "sender=" + sender + ", " +
                "lamp=" + lamp + ", " +
                "messageSender=" + messageSender + ", " +
                "errorSender=" + errorSender + ']';
    }

}
package revxrsal.commands.sponge.actor;

import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

final class BasicSpongeActor implements SpongeCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private final CommandCause sender;
    private final Lamp<SpongeCommandActor> lamp;
    private final MessageSender<SpongeCommandActor, ComponentLike> messageSender;
    private final MessageSender<SpongeCommandActor, ComponentLike> errorSender;

    BasicSpongeActor(
            CommandCause sender,
            Lamp<SpongeCommandActor> lamp,
            MessageSender<SpongeCommandActor, ComponentLike> messageSender,
            MessageSender<SpongeCommandActor, ComponentLike> errorSender
    ) {
        this.sender = sender;
        this.lamp = lamp;
        this.messageSender = messageSender;
        this.errorSender = errorSender;
    }

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

    public CommandCause sender() {return sender;}

    public MessageSender<SpongeCommandActor, ComponentLike> messageSender() {return messageSender;}

    public MessageSender<SpongeCommandActor, ComponentLike> errorSender() {return errorSender;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BasicSpongeActor that = (BasicSpongeActor) obj;
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
        return "BasicSpongeActor[" +
                "sender=" + sender + ", " +
                "lamp=" + lamp + ", " +
                "messageSender=" + messageSender + ", " +
                "errorSender=" + errorSender + ']';
    }

}
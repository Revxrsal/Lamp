package revxrsal.commands.fabric.actor;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

final class BasicFabricActor implements FabricCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private final ServerCommandSource sender;
    private final Lamp<FabricCommandActor> lamp;
    private final MessageSender<FabricCommandActor, Text> messageSender;
    private final MessageSender<FabricCommandActor, Text> errorSender;

    BasicFabricActor(
            ServerCommandSource sender,
            Lamp<FabricCommandActor> lamp,
            MessageSender<FabricCommandActor, Text> messageSender,
            MessageSender<FabricCommandActor, Text> errorSender
    ) {
        this.sender = sender;
        this.lamp = lamp;
        this.messageSender = messageSender;
        this.errorSender = errorSender;
    }

    @Override public @NotNull ServerCommandSource source() {
        return sender;
    }

    @Override public void reply(@NotNull Text message) {
        messageSender.send(this, message);
    }

    @Override public void error(@NotNull Text message) {
        errorSender.send(this, message);
    }

    @Override public @NotNull UUID uniqueId() {
        if (sender.getEntity() != null)
            return sender.getEntity().getUuid();
        else if (isConsole())
            return CONSOLE_UUID;
        else
            return UUID.nameUUIDFromBytes(name().getBytes(StandardCharsets.UTF_8));
    }

    @Override public Lamp<FabricCommandActor> lamp() {
        return lamp;
    }

    public ServerCommandSource sender() {return sender;}

    public MessageSender<FabricCommandActor, Text> messageSender() {return messageSender;}

    public MessageSender<FabricCommandActor, Text> errorSender() {return errorSender;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BasicFabricActor) obj;
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
        return "BasicFabricActor[" +
                "sender=" + sender + ", " +
                "lamp=" + lamp + ", " +
                "messageSender=" + messageSender + ", " +
                "errorSender=" + errorSender + ']';
    }

}
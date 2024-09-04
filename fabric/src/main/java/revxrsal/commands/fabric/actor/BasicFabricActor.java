package revxrsal.commands.fabric.actor;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

record BasicFabricActor(
        ServerCommandSource sender,
        Lamp<FabricCommandActor> lamp,
        MessageSender<FabricCommandActor, Text> messageSender,
        MessageSender<FabricCommandActor, Text> errorSender
) implements FabricCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

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
}
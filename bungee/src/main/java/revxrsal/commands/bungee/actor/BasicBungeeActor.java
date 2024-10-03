package revxrsal.commands.bungee.actor;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

final class BasicBungeeActor implements BungeeCommandActor {
    private final CommandSender sender;
    private final Lamp<BungeeCommandActor> lamp;

    BasicBungeeActor(CommandSender sender, Lamp<BungeeCommandActor> lamp) {
        this.sender = sender;
        this.lamp = lamp;
    }

    @Override public @NotNull CommandSender sender() {
        return sender;
    }

    @Override public @NotNull UUID uniqueId() {
        if (isPlayer())
            return ((ProxiedPlayer) sender).getUniqueId();
        else
            return UUID.nameUUIDFromBytes(name().getBytes(StandardCharsets.UTF_8));
    }

    @Override public Lamp<BungeeCommandActor> lamp() {
        return lamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BasicBungeeActor that = (BasicBungeeActor) obj;
        return Objects.equals(this.sender, that.sender) &&
                Objects.equals(this.lamp, that.lamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, lamp);
    }

    @Override
    public String toString() {
        return "BasicBungeeActor[" +
                "sender=" + sender + ", " +
                "lamp=" + lamp + ']';
    }

}
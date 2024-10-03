package revxrsal.commands.bukkit.actor;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class BasicBukkitActor implements BukkitCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private final CommandSender sender;
    private final Plugin plugin;
    private final Optional<BukkitAudiences> audiences;
    private final MessageSender<BukkitCommandActor, ComponentLike> messageSender;
    private final Lamp<BukkitCommandActor> lamp;

    BasicBukkitActor(
            CommandSender sender,
            Plugin plugin,
            Optional<BukkitAudiences> audiences,
            MessageSender<BukkitCommandActor, ComponentLike> messageSender,
            Lamp<BukkitCommandActor> lamp
    ) {
        this.sender = sender;
        this.plugin = plugin;
        this.audiences = audiences;
        this.messageSender = messageSender;
        this.lamp = lamp;
    }

    @Override public @NotNull CommandSender sender() {
        return sender;
    }

    @Override public void reply(@NotNull ComponentLike message) {
        if (messageSender == null)
            audience().ifPresent(a -> a.sendMessage(message));
        else
            messageSender.send(this, message);
    }

    @Override public @NotNull Optional<Audience> audience() {
        //noinspection DataFlowIssue
        if (sender instanceof Audience)
            return Optional.of((Audience) sender);
        if (!audiences.isPresent())
            return Optional.empty();
        BukkitAudiences bukkitAudiences = audiences.get();
        return Optional.of(bukkitAudiences.sender(sender()));
    }

    @Override public @NotNull UUID uniqueId() {
        if (isPlayer())
            return ((Player) sender).getUniqueId();
        else if (isConsole())
            return CONSOLE_UUID;
        else
            return UUID.nameUUIDFromBytes(name().getBytes(StandardCharsets.UTF_8));
    }

    @Override public Lamp<BukkitCommandActor> lamp() {
        return lamp;
    }

    public Plugin plugin() {return plugin;}

    public Optional<BukkitAudiences> audiences() {return audiences;}

    public MessageSender<BukkitCommandActor, ComponentLike> messageSender() {return messageSender;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BasicBukkitActor that = (BasicBukkitActor) obj;
        return Objects.equals(this.sender, that.sender) &&
                Objects.equals(this.plugin, that.plugin) &&
                Objects.equals(this.audiences, that.audiences) &&
                Objects.equals(this.messageSender, that.messageSender) &&
                Objects.equals(this.lamp, that.lamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, plugin, audiences, messageSender, lamp);
    }

    @Override
    public String toString() {
        return "BasicBukkitActor[" +
                "sender=" + sender + ", " +
                "plugin=" + plugin + ", " +
                "audiences=" + audiences + ", " +
                "messageSender=" + messageSender + ", " +
                "lamp=" + lamp + ']';
    }

}
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
import java.util.Optional;
import java.util.UUID;

record BasicBukkitActor(
        CommandSender sender,
        Plugin plugin,
        Optional<BukkitAudiences> audiences,
        MessageSender<BukkitCommandActor, ComponentLike> messageSender,
        Lamp<BukkitCommandActor> lamp
) implements BukkitCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

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
            return Optional.of(sender);
        if (audiences.isEmpty())
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
}
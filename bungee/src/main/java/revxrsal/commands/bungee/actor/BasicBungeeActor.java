package revxrsal.commands.bungee.actor;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

record BasicBungeeActor(CommandSender sender, Lamp<BungeeCommandActor> lamp) implements BungeeCommandActor {

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
}
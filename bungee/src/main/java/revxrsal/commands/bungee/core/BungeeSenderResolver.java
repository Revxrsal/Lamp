package revxrsal.commands.bungee.core;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.SenderResolver;

enum BungeeSenderResolver implements SenderResolver {
    INSTANCE;

    @Override public boolean isCustomType(Class<?> type) {
        return CommandSender.class.isAssignableFrom(type);
    }

    @Override public @NotNull Object getSender(@NotNull Class<?> customSenderType, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
        if (ProxiedPlayer.class.isAssignableFrom(customSenderType)) {
            return ((BungeeCommandActor) actor).requirePlayer();
        }
        return ((BungeeCommandActor) actor).getSender();
    }
}

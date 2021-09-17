package revxrsal.commands.bukkit.core;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.SenderResolver;
import revxrsal.commands.bukkit.BukkitCommandActor;

enum BukkitSenderResolver implements SenderResolver {

    INSTANCE;

    @Override public boolean isCustomType(Class<?> type) {
        return CommandSender.class.isAssignableFrom(type);
    }

    @Override public @NotNull Object getSender(@NotNull Class<?> customSenderType, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
        BukkitCommandActor bActor = (BukkitCommandActor) actor;
        if (Player.class.isAssignableFrom(customSenderType)) {
            return bActor.requirePlayer();
        }
        if (ConsoleCommandSender.class.isAssignableFrom(customSenderType)) {
            return bActor.requireConsole();
        }
        return bActor.getSender();
    }
}

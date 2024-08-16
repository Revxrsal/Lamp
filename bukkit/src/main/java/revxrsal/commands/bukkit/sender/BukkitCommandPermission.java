package revxrsal.commands.bukkit.sender;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.command.CommandPermission;

/**
 * A Bukkit-adapted wrapper for {@link CommandPermission}
 */
public record BukkitCommandPermission(@NotNull Permission permission) implements CommandPermission<BukkitCommandActor> {

    @Override public boolean isExecutableBy(@NotNull BukkitCommandActor actor) {
        return actor.sender().hasPermission(permission);
    }
}

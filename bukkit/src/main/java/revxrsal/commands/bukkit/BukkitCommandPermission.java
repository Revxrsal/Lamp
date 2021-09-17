package revxrsal.commands.bukkit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandPermission;

/**
 * A Bukkit-adapted wrapper for {@link CommandPermission}
 */
@Getter
@ToString
@AllArgsConstructor
public final class BukkitCommandPermission implements CommandPermission {

    /**
     * The permission node
     */
    private final @NotNull Permission permission;

    /**
     * Returns whether the sender has permission to use this command
     * or not.
     *
     * @param actor Actor to test against
     * @return {@code true} if they can use it, false if otherwise.
     */
    @Override public boolean canExecute(@NotNull CommandActor actor) {
        return ((BukkitCommandActor) actor).getSender().hasPermission(permission);
    }
}

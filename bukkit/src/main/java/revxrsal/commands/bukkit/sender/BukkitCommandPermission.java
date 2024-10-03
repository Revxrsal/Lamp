package revxrsal.commands.bukkit.sender;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.command.CommandPermission;

import java.util.Objects;

/**
 * A Bukkit-adapted wrapper for {@link CommandPermission}
 */
public final class BukkitCommandPermission implements CommandPermission<BukkitCommandActor> {
    private final @NotNull Permission permission;

    /**
     *
     */
    public BukkitCommandPermission(@NotNull Permission permission) {this.permission = permission;}

    @Override public boolean isExecutableBy(@NotNull BukkitCommandActor actor) {
        return actor.sender().hasPermission(permission);
    }

    public @NotNull Permission permission() {return permission;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BukkitCommandPermission that = (BukkitCommandPermission) obj;
        return Objects.equals(this.permission, that.permission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permission);
    }

    @Override
    public String toString() {
        return "BukkitCommandPermission[" +
                "permission=" + permission + ']';
    }

}

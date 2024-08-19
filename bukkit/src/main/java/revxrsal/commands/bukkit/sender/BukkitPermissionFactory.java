package revxrsal.commands.bukkit.sender;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public enum BukkitPermissionFactory implements revxrsal.commands.command.CommandPermission.Factory<BukkitCommandActor> {
    INSTANCE;

    @Override
    public @Nullable revxrsal.commands.command.CommandPermission<BukkitCommandActor> create(@NotNull AnnotationList annotations, @NotNull Lamp<BukkitCommandActor> lamp) {
        CommandPermission permissionAnn = annotations.get(revxrsal.commands.bukkit.annotation.CommandPermission.class);
        if (permissionAnn == null)
            return null;
        return new BukkitCommandPermission(new Permission(permissionAnn.value(), permissionAnn.defaultAccess()));
    }
}

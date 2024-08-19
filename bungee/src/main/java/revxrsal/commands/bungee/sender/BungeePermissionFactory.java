package revxrsal.commands.bungee.sender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;

public enum BungeePermissionFactory implements revxrsal.commands.command.CommandPermission.Factory<BungeeCommandActor> {
    INSTANCE;

    @Override
    public @Nullable revxrsal.commands.command.CommandPermission<BungeeCommandActor> create(@NotNull AnnotationList annotations, @NotNull Lamp<BungeeCommandActor> lamp) {
        CommandPermission permissionAnn = annotations.get(CommandPermission.class);
        if (permissionAnn == null)
            return null;
        return actor -> actor.sender().hasPermission(permissionAnn.value());
    }
}

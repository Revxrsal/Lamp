package revxrsal.commands.minestom.sender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.minestom.actor.MinestomCommandActor;
import revxrsal.commands.minestom.annotation.CommandPermission;

public enum MinestomPermissionFactory implements revxrsal.commands.command.CommandPermission.Factory<MinestomCommandActor> {
    INSTANCE;

    @Override
    public @Nullable revxrsal.commands.command.CommandPermission<MinestomCommandActor> create(@NotNull AnnotationList annotations, @NotNull Lamp<MinestomCommandActor> lamp) {
        CommandPermission permissionAnn = annotations.get(CommandPermission.class);
        if (permissionAnn == null)
            return null;
        return actor -> actor.sender().hasPermission(permissionAnn.value());
    }
}

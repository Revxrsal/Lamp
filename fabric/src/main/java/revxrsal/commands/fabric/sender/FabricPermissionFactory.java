package revxrsal.commands.fabric.sender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.fabric.actor.FabricCommandActor;
import revxrsal.commands.fabric.annotation.CommandPermission;

public enum FabricPermissionFactory implements revxrsal.commands.command.CommandPermission.Factory<FabricCommandActor> {
    INSTANCE;

    @Override
    public @Nullable revxrsal.commands.command.CommandPermission<FabricCommandActor> create(@NotNull AnnotationList annotations, @NotNull Lamp<FabricCommandActor> lamp) {
        CommandPermission permissionAnn = annotations.get(CommandPermission.class);
        if (permissionAnn == null)
            return null;
        return actor -> actor.source().hasPermissionLevel(permissionAnn.value());
    }
}

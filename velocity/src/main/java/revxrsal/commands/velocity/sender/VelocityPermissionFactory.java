package revxrsal.commands.velocity.sender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.velocity.actor.VelocityCommandActor;
import revxrsal.commands.velocity.annotation.CommandPermission;

public enum VelocityPermissionFactory implements revxrsal.commands.command.CommandPermission.Factory<VelocityCommandActor> {
    INSTANCE;

    @Override
    public @Nullable revxrsal.commands.command.CommandPermission<VelocityCommandActor> create(@NotNull AnnotationList annotations, @NotNull Lamp<VelocityCommandActor> lamp) {
        CommandPermission permissionAnn = annotations.get(CommandPermission.class);
        if (permissionAnn == null)
            return null;
        return actor -> actor.source().hasPermission(permissionAnn.value());
    }
}

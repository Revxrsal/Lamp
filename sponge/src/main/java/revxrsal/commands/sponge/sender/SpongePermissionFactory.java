package revxrsal.commands.sponge.sender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.sponge.actor.SpongeCommandActor;
import revxrsal.commands.sponge.annotation.CommandPermission;

public enum SpongePermissionFactory implements revxrsal.commands.command.CommandPermission.Factory<SpongeCommandActor> {
    INSTANCE;

    @Override
    public @Nullable revxrsal.commands.command.CommandPermission<SpongeCommandActor> create(@NotNull AnnotationList annotations, @NotNull Lamp<SpongeCommandActor> lamp) {
        CommandPermission permissionAnn = annotations.get(revxrsal.commands.sponge.annotation.CommandPermission.class);
        if (permissionAnn == null)
            return null;
        return actor -> actor.cause().hasPermission(permissionAnn.value());
    }
}

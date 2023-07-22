package revxrsal.commands.sponge.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.trait.CommandAnnotationHolder;
import revxrsal.commands.process.PermissionReader;
import revxrsal.commands.sponge.SpongeCommandPermission;
import revxrsal.commands.sponge.annotation.CommandPermission;

enum SpongePermissionReader implements PermissionReader {
    INSTANCE;

    @Override public @Nullable revxrsal.commands.command.CommandPermission getPermission(@NotNull CommandAnnotationHolder command) {
        CommandPermission permissionAnn = command.getAnnotation(CommandPermission.class);
        if (permissionAnn == null)
            return null;
        return new SpongeCommandPermission(permissionAnn.value());
    }
}

package revxrsal.commands.velocity.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.trait.CommandAnnotationHolder;
import revxrsal.commands.process.PermissionReader;
import revxrsal.commands.velocity.VelocityCommandPermission;
import revxrsal.commands.velocity.annotation.CommandPermission;

enum VelocityPermissionReader implements PermissionReader {
    INSTANCE;

    @Override public @Nullable revxrsal.commands.command.CommandPermission getPermission(@NotNull CommandAnnotationHolder command) {
        CommandPermission permissionAnn = command.getAnnotation(CommandPermission.class);
        if (permissionAnn == null)
            return null;
        return new VelocityCommandPermission(permissionAnn.value());
    }
}

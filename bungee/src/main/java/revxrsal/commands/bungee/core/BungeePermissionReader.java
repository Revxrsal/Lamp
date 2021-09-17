package revxrsal.commands.bungee.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bungee.BungeeCommandPermission;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.PermissionReader;

enum BungeePermissionReader implements PermissionReader {
    INSTANCE;

    @Override public @Nullable revxrsal.commands.command.CommandPermission getPermission(@NotNull ExecutableCommand command) {
        CommandPermission permissionAnn = command.getAnnotation(CommandPermission.class);
        if (permissionAnn == null)
            return null;
        return new BungeeCommandPermission(permissionAnn.value());
    }
}

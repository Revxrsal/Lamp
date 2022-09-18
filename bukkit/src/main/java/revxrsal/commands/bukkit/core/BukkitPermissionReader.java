package revxrsal.commands.bukkit.core;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bukkit.BukkitCommandPermission;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.command.trait.CommandAnnotationHolder;
import revxrsal.commands.process.PermissionReader;

enum BukkitPermissionReader implements PermissionReader {
  INSTANCE;

  @Override
  public @Nullable revxrsal.commands.command.CommandPermission getPermission(
      @NotNull CommandAnnotationHolder command) {
    CommandPermission permissionAnn = command.getAnnotation(CommandPermission.class);
    if (permissionAnn == null) {
      return null;
    }
    return new BukkitCommandPermission(
        new Permission(permissionAnn.value(), permissionAnn.defaultAccess()));
  }
}

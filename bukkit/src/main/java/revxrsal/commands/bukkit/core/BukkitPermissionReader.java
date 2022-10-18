package revxrsal.commands.bukkit.core;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bukkit.BukkitCommandPermission;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.trait.CommandAnnotationHolder;
import revxrsal.commands.process.PermissionReader;

enum BukkitPermissionReader implements PermissionReader {
  INSTANCE;

  @Override
  public @Nullable CommandPermission getPermission(
      @NotNull CommandAnnotationHolder command) {
    revxrsal.commands.bukkit.annotation.CommandPermission permissionAnn = command.getAnnotation(
        revxrsal.commands.bukkit.annotation.CommandPermission.class);
    if (permissionAnn == null) {
      return null;
    }
    return new BukkitCommandPermission(
        new Permission(permissionAnn.value(), permissionAnn.defaultAccess()));
  }
}

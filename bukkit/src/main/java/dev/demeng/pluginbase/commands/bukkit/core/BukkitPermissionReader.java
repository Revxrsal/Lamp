package dev.demeng.pluginbase.commands.bukkit.core;

import dev.demeng.pluginbase.commands.bukkit.BukkitCommandPermission;
import dev.demeng.pluginbase.commands.command.CommandPermission;
import dev.demeng.pluginbase.commands.command.trait.CommandAnnotationHolder;
import dev.demeng.pluginbase.commands.process.PermissionReader;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum BukkitPermissionReader implements PermissionReader {
  INSTANCE;

  @Override
  public @Nullable CommandPermission getPermission(
      @NotNull CommandAnnotationHolder command) {
    dev.demeng.pluginbase.commands.bukkit.annotation.CommandPermission permissionAnn = command.getAnnotation(
        dev.demeng.pluginbase.commands.bukkit.annotation.CommandPermission.class);
    if (permissionAnn == null) {
      return null;
    }
    return new BukkitCommandPermission(
        new Permission(permissionAnn.value(), permissionAnn.defaultAccess()));
  }
}

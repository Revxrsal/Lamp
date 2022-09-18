package dev.demeng.pluginbase.commands.bukkit.core;

import dev.demeng.pluginbase.commands.bukkit.BukkitCommandActor;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.process.SenderResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

enum BukkitSenderResolver implements SenderResolver {

  INSTANCE;

  @Override
  public boolean isCustomType(Class<?> type) {
    return CommandSender.class.isAssignableFrom(type);
  }

  @Override
  public @NotNull Object getSender(@NotNull Class<?> customSenderType, @NotNull CommandActor actor,
      @NotNull ExecutableCommand command) {
    BukkitCommandActor bActor = (BukkitCommandActor) actor;
    if (Player.class.isAssignableFrom(customSenderType)) {
      return bActor.requirePlayer();
    }
    if (ConsoleCommandSender.class.isAssignableFrom(customSenderType)) {
      return bActor.requireConsole();
    }
    return bActor.getSender();
  }
}

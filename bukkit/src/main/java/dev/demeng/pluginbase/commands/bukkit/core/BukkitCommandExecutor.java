package dev.demeng.pluginbase.commands.bukkit.core;

import dev.demeng.pluginbase.commands.bukkit.BukkitCommandActor;
import dev.demeng.pluginbase.commands.command.ArgumentStack;
import dev.demeng.pluginbase.commands.exception.ArgumentParseException;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class BukkitCommandExecutor implements TabExecutor {

  private final BukkitHandler handler;

  public BukkitCommandExecutor(BukkitHandler handler) {
    this.handler = handler;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    BukkitCommandActor actor = new BukkitActor(sender, handler);
    try {
      ArgumentStack arguments = handler.parseArguments(args);
      arguments.addFirst(command.getName());

      handler.dispatch(actor, arguments);
    } catch (Throwable t) {
      handler.getExceptionHandler().handleException(t, actor);
    }
    return true;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {
    try {
      BukkitCommandActor actor = new BukkitActor(sender, handler);
      ArgumentStack arguments = handler.parseArgumentsForCompletion(args);

      arguments.addFirst(command.getName());
      return handler.getAutoCompleter().complete(actor, arguments);
    } catch (ArgumentParseException e) {
      return Collections.emptyList();
    }
  }
}

package revxrsal.commands.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;

final class BaseCommandCategory implements CommandCategory {

  // lazily populated by CommandParser
  CommandPath path;
  String name;
  @Nullable BaseCommandCategory parent;
  @Nullable CommandExecutable defaultAction;
  CommandHandler handler;

  final Map<CommandPath, ExecutableCommand> commands = new HashMap<>();
  final Map<CommandPath, BaseCommandCategory> categories = new HashMap<>();
  final CommandPermission permission = new CategoryPermission();

  @Override
  public @NotNull String getName() {
    return name;
  }

  @Override
  public @NotNull CommandPath getPath() {
    return path;
  }

  @Override
  public @NotNull CommandHandler getCommandHandler() {
    return handler;
  }

  @Override
  public @Nullable CommandCategory getParent() {
    return parent;
  }

  @Override
  public @Nullable ExecutableCommand getDefaultAction() {
    return defaultAction;
  }

  @Override
  public @NotNull CommandPermission getPermission() {
    return permission;
  }

  @Override
  public boolean isSecret() {
    for (ExecutableCommand command : commands.values()) {
      if (command.isSecret()) {
        continue;
      }
      return false;
    }
    for (CommandCategory category : categories.values()) {
      if (category.isSecret()) {
        continue;
      }
      return false;
    }
    return true;
  }

  @Override
  public boolean isEmpty() {
    return defaultAction == null && commands.isEmpty() && categories.isEmpty();
  }

  private final Map<CommandPath, CommandCategory> unmodifiableCategories = Collections.unmodifiableMap(
      categories);

  @Override
  public @NotNull @UnmodifiableView Map<CommandPath, CommandCategory> getCategories() {
    return unmodifiableCategories;
  }

  private final Map<CommandPath, ExecutableCommand> unmodifiableCommands = Collections.unmodifiableMap(
      commands);

  @Override
  public @NotNull @UnmodifiableView Map<CommandPath, ExecutableCommand> getCommands() {
    return unmodifiableCommands;
  }

  @Override
  public String toString() {
    return "CommandCategory{path=" + path + ", name='" + name + "'}";
  }

  public void parent(BaseCommandCategory cat) {
    parent = cat;
    if (cat != null) {
      cat.categories.put(path, this);
    }
  }

  /**
   * Category permission: They have access to the category if they have access to any of its
   * commands or other categories.
   */
  private class CategoryPermission implements CommandPermission {

    @Override
    public boolean canExecute(@NotNull CommandActor actor) {
      for (ExecutableCommand command : commands.values()) {
        if (command.getPermission().canExecute(actor)) {
          return true;
        }
      }
      for (CommandCategory category : categories.values()) {
        if (category.getPermission().canExecute(actor)) {
          return true;
        }
      }
      if (defaultAction == null) {
        return false;
      }
      return defaultAction.hasPermission(actor);
    }
  }

  @Override
  public int compareTo(@NotNull CommandCategory o) {
    return path.compareTo(o.getPath());
  }
}

package dev.demeng.pluginbase.commands.command;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a permission that is required in order to execute a command.
 * <p>
 * This implementation may vary depending on the target platform
 */
public interface CommandPermission {

  /**
   * A {@link CommandPermission} that returns true for any sender.
   */
  CommandPermission ALWAYS_TRUE = actor -> true;

  /**
   * Returns whether the sender has permission to use this command or not.
   *
   * @param actor Actor to test against
   * @return {@code true} if they can use it, false if otherwise.
   */
  boolean canExecute(@NotNull CommandActor actor);

}

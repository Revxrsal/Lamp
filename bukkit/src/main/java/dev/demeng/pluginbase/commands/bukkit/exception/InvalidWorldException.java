package dev.demeng.pluginbase.commands.bukkit.exception;

import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.exception.InvalidValueException;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an invalid value for a {@link org.bukkit.World} parameter is inputted in the command
 */
public class InvalidWorldException extends InvalidValueException {

  public InvalidWorldException(@NotNull CommandParameter parameter, @NotNull String input) {
    super(parameter, input);
  }
}

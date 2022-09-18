package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an invalid value is inputted for a {@code boolean} parameter
 */
public class InvalidBooleanException extends InvalidValueException {

  public InvalidBooleanException(@NotNull CommandParameter parameter, @NotNull String input) {
    super(parameter, input);
  }
}

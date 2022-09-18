package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an invalid value is supplied for a number-like parameter.
 */
public class InvalidNumberException extends InvalidValueException {

  public InvalidNumberException(@NotNull CommandParameter parameter, @NotNull String input) {
    super(parameter, input);
  }
}

package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an invalid value is supplied for a {@link java.util.UUID} parameter.
 */
public class InvalidUUIDException extends InvalidValueException {

  public InvalidUUIDException(@NotNull CommandParameter parameter, @NotNull String input) {
    super(parameter, input);
  }
}

package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;

/**
 * Thrown when an invalid value is supplied for a number-like parameter.
 */
public class InvalidNumberException extends InvalidValueException {

  public InvalidNumberException(@NotNull CommandParameter parameter, @NotNull String input) {
    super(parameter, input);
  }
}

package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.command.CommandParameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an unacceptable value for a certain parameter is inputted, for example, an invalid
 * number for a number parameter, or an invalid UUID for a {@link java.util.UUID} parameter.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public abstract class InvalidValueException extends RuntimeException {

  /**
   * The parameter being resolved
   */
  private final @NotNull CommandParameter parameter;

  /**
   * The invalid inputted value for the number
   */
  private final @NotNull String input;

}

package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an invalid value is specified for an {@link Enum} parameter.
 */
public class EnumNotFoundException extends InvalidValueException {

  public EnumNotFoundException(@NotNull CommandParameter parameter, @NotNull String input) {
    super(parameter, input);
  }
}

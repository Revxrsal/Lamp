package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an invalid value is supplied for a {@link java.net.URI} or a {@link java.net.URL}
 * parameter.
 */
public class InvalidURLException extends InvalidValueException {

  public InvalidURLException(@NotNull CommandParameter parameter, @NotNull String input) {
    super(parameter, input);
  }
}

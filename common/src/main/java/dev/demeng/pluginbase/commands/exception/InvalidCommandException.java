package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.core.CommandPath;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an invalid root command is inputted.
 * <p>
 * For sub-commands, see {@link InvalidSubcommandException}.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class InvalidCommandException extends RuntimeException {

  /**
   * The inputted path
   */
  private final @NotNull CommandPath path;

  /**
   * The inputted command name. This does not include the whole input.
   */
  private final @NotNull String input;

}

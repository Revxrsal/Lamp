package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.core.CommandPath;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an invalid subcommand is inputted.
 * <p>
 * For root commands, see {@link InvalidCommandException}.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class InvalidSubcommandException extends RuntimeException {

  /**
   * The inputted path
   */
  private final @NotNull CommandPath path;

  /**
   * The inputted command name. This does not include the whole input.
   */
  private final @NotNull String input;

}

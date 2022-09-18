package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.command.ArgumentStack;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when extra arguments are inputted for a command. By default, this exception is not thrown,
 * unless specifend by {@link CommandHandler#failOnTooManyArguments()}.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class TooManyArgumentsException extends RuntimeException {

  /**
   * The command being executed
   */
  private final @NotNull ExecutableCommand command;

  /**
   * The extra arguments
   */
  private final @NotNull ArgumentStack arguments;
}

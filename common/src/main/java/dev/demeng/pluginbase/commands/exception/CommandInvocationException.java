package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an exception is thrown by invoking the command's method.
 * <p>
 * This exception will <strong>always</strong> wrap any exceptions thrown during the command
 * invocation, excluding any exception annotated with {@link ThrowableFromCommand}, which will get
 * unwrapped.
 */
@Getter
@AllArgsConstructor
public class CommandInvocationException extends RuntimeException {

  /**
   * The command being executed
   */
  private final @NotNull ExecutableCommand command;

  /**
   * The underlying error in the command
   */
  private final @NotNull Throwable cause;

}

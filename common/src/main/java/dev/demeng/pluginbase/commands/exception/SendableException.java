package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.command.CommandActor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an exception that is purely used to send messages directly to the sender. Exceptions
 * of this type are <strong>not</strong> handled by exception handlers, and instead get their
 * {@link #sendTo(CommandActor)} method invoked directly.
 */
@ThrowableFromCommand
public abstract class SendableException extends RuntimeException {

  /**
   * Constructs a new {@link SendableException} with an inferred actor
   *
   * @param message Message to send
   */
  public SendableException(String message) {
    super(message);
  }

  /**
   * Sends the message to the given actor
   *
   * @param actor Actor to send to
   */
  public abstract void sendTo(@NotNull CommandActor actor);

}

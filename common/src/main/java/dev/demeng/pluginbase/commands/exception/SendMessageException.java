package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.command.CommandActor;
import org.jetbrains.annotations.NotNull;

/**
 * Used to directly send-and-return a message to the command actor.
 * <p>
 * This exception should be used to directly transfer messages to the {@link CommandActor}, however
 * should not be used in command-fail contexts. To signal an error to the actor, use
 * {@link CommandErrorException}.
 */
public class SendMessageException extends SendableException {

  private final Object[] arguments;

  /**
   * Constructs a new {@link SendMessageException} with an inferred actor
   *
   * @param message Message to send
   */
  public SendMessageException(String message, Object... arguments) {
    super(message);
    this.arguments = arguments;
  }

  /**
   * Sends the message to the given actor
   *
   * @param actor Actor to send to
   */
  @Override
  public void sendTo(@NotNull CommandActor actor) {
    actor.replyLocalized(getMessage(), arguments);
  }
}

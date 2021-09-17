package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;

/**
 * Used to directly send-and-return a message to the command actor.
 * <p>
 * This exception should be used to directly transfer messages to the {@link CommandActor},
 * however should not be used in command-fail contexts. To signal an error to the
 * actor, use {@link CommandErrorException}.
 * <p>
 * The command actor, in most cases, can be left null, however
 * in exceptions thrown in certain circumstances it may be impossible
 * to infer the actor, in which case it must be explicitly specified.
 */
public class SendMessageException extends SendableException {

    /**
     * Constructs a new {@link SendMessageException} with an inferred actor
     *
     * @param message Message to send
     */
    public SendMessageException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link SendMessageException} with a specified actor
     *
     * @param actor   Actor to send to
     * @param message Message to send
     */
    public SendMessageException(@Nullable CommandActor actor, String message) {
        super(actor, message);
    }

    /**
     * Sends the message to the given actor
     *
     * @param actor Actor to send to
     */
    @Override public void sendTo(@NotNull CommandActor actor) {
        actor.reply(getMessage());
    }
}

package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;

/**
 * Represents a command error, for example when an invalid value is returned, a condition
 * is violated, or an illegal value is inputted, and validated through a parameter validator.
 * <p>
 * This exception should be used to directly transfer error messages to the {@link CommandActor},
 * and is always used in command-fail contexts. For exceptions that only reply to the
 * actor, see {@link SendMessageException}.
 */
public class CommandErrorException extends SendableException {

    /**
     * Constructs a new {@link CommandErrorException} with an inferred actor
     *
     * @param message Message to send
     */
    public CommandErrorException(String message) {
        super(message);
    }

    /**
     * Sends the message to the given actor
     *
     * @param actor Actor to send to
     */
    @Override public void sendTo(@NotNull CommandActor actor) {
        actor.error(getMessage());
    }
}

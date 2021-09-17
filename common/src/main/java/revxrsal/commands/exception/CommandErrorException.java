package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;

/**
 * Represents a command error, for example when an invalid value is returned, a condition
 * is violated, or an illegal value inputted, and validated through a parameter validator.
 * <p>
 * This exception should be used to directly transfer error messages to the {@link CommandActor},
 * and is always used in command-fail contexts. For exceptions that only reply to the
 * actor, see {@link SendMessageException}.
 * <p>
 * The command actor, in most cases, can be left null, however
 * in exceptions thrown in certain circumstances it may be impossible
 * to infer the actor, in which case it must be explicitly specified.
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
     * Constructs a new {@link CommandErrorException} with a specified actor
     *
     * @param actor   Actor to send to
     * @param message Message to send
     */
    public CommandErrorException(@Nullable CommandActor actor, String message) {
        super(actor, message);
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

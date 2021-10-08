package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;

/**
 * A handler for post-handling command responses (results returned from the
 * command methods)
 *
 * @param <T> The response type
 */
public interface ResponseHandler<T> {

    /**
     * Handles the response returned from the method
     *
     * @param response The response returned from the method. May or may
     *                 not be null.
     * @param actor    The actor of the command
     * @param command  The command being executed
     */
    void handleResponse(T response, @NotNull CommandActor actor, @NotNull ExecutableCommand command);

    /**
     * A utility method that directly replies with the response.
     * <p>
     * This is intended to be used as a method reference: {@code ResponseHandler::reply}
     *
     * @param response Response to handle
     * @param actor    The command actor
     * @param command  The command being executed
     */
    static void reply(Object response, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
        actor.reply(String.valueOf(response));
    }
}

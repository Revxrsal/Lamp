package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandActor;

/**
 * A handler for all exceptions that may be thrown during the command
 * invocation.
 * <p>
 * Exceptions may be anything, including Java's normal exceptions,
 * and build-in ones thrown by different components in the framework.
 * <p>
 * Set with {@link CommandHandler#setExceptionHandler(CommandExceptionHandler)}.
 */
public interface CommandExceptionHandler {

    /**
     * Handles the given exception. Note that this method does not
     * provide information about the command context (such as the command, etc.)
     * These are available in individual exceptions and can be
     * accessed only if the thrown exception exposes them.
     *
     * @param throwable Exception to handle
     * @param actor     The command actor
     */
    void handleException(@NotNull Throwable throwable, @NotNull CommandActor actor);

}

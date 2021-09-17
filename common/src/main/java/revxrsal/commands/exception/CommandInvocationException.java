package revxrsal.commands.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;

/**
 * Thrown when an exception is thrown by invoking the command's method.
 * <p>
 * Note that this does not include {@link SendMessageException} or {@link CommandErrorException},
 * which are purely for sending messages directly to the actor, hence they are treated in
 * a special way.
 * <p>
 * Beside the aforementioned exceptions, this exception will <strong>always</strong> wrap
 * any exceptions thrown during the command invocation, excluding any exception
 * annotated with {@link ThrowableFromCommand}, which will get unwrapped.
 */
@Getter
@AllArgsConstructor
public class CommandInvocationException extends RuntimeException {

    /**
     * The command actor
     */
    private final @NotNull CommandActor actor;

    /**
     * The command being executed
     */
    private final @NotNull ExecutableCommand command;

    /**
     * The underlying error in the command
     */
    private final @NotNull Throwable cause;

}

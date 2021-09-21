package revxrsal.commands.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;

/**
 * Thrown when a {@link CommandActor} lacks the required permission to
 * execute the given command
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class NoPermissionException extends RuntimeException {

    /**
     * The command being executed
     */
    private final @NotNull ExecutableCommand command;

    /**
     * The permission the actor lacks
     */
    private final @NotNull CommandPermission permission;

}

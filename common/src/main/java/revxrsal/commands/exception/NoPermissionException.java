package revxrsal.commands.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;

/**
 * Thrown when a {@link CommandActor} lacks the required permission to
 * execute the given command or category.
 * <p>
 * Note that {@link #getCommand()} may be null when the user attempts
 * to access a category in which they do not have permission.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class NoPermissionException extends RuntimeException {

    /**
     * The command being executed
     */
    private final @Nullable ExecutableCommand command;

    /**
     * The category being accessed.
     */
    private final CommandCategory category;

    /**
     * The permission the actor lacks
     */
    private final @NotNull CommandPermission permission;

}

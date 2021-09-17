package revxrsal.commands.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.core.CommandPath;

/**
 * Thrown when an invalid root command is inputted.
 * <p>
 * For sub-commands, see {@link InvalidSubcommandException}.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class InvalidCommandException extends RuntimeException {

    /**
     * The command actor
     */
    private final @NotNull CommandActor actor;

    /**
     * The inputted path
     */
    private final @NotNull CommandPath path;

    /**
     * The inputted command name. This does not include the whole input.
     */
    private final @NotNull String input;

}

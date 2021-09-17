package revxrsal.commands.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.core.CommandPath;

/**
 * Thrown when an invalid subcommand is inputted.
 * <p>
 * For root commands, see {@link InvalidCommandException}.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class InvalidSubcommandException extends RuntimeException {

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

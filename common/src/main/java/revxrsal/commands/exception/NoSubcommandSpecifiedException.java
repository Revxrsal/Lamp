package revxrsal.commands.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.annotation.Default;

/**
 * Thrown when no subcommand is specified, as in, when the command input
 * ends to a category and not an executable command.
 * <p>
 * Note that this exception will not be thrown if the category
 * specifies a {@link Default} method.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class NoSubcommandSpecifiedException extends RuntimeException {

    /**
     * The command actor
     */
    private final @NotNull CommandActor actor;

    /**
     * The category that is inputted
     */
    private final @NotNull CommandCategory category;

}

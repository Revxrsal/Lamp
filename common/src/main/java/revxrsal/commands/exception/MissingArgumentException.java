package revxrsal.commands.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;

/**
 * Thrown when a parameter is missing (not specified) inside a command
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class MissingArgumentException extends RuntimeException {

    /**
     * The parameter that is missing
     */
    private final @NotNull CommandParameter parameter;

    /**
     * Returns the command being executed
     *
     * @return The command
     */
    public @NotNull ExecutableCommand getCommand() {
        return parameter.getDeclaringCommand();
    }

}

package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;

/**
 * Thrown when an invalid value is inputted for a {@code boolean} parameter
 */
public class InvalidBooleanException extends InvalidValueException {

    public InvalidBooleanException(@NotNull CommandParameter parameter, @NotNull String input) {
        super(parameter, input);
    }
}

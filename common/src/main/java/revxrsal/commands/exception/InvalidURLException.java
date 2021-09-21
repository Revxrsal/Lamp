package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;

/**
 * Thrown when an invalid value is supplied for a {@link java.net.URI} or
 * a {@link java.net.URL} parameter.
 */
public class InvalidURLException extends InvalidValueException {

    public InvalidURLException(@NotNull CommandParameter parameter, @NotNull String input) {
        super(parameter, input);
    }
}

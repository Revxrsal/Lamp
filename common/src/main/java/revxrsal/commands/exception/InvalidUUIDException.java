package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;

/**
 * Thrown when an invalid value is supplied for a {@link java.util.UUID} parameter.
 */
public class InvalidUUIDException extends InvalidValueException {

    public InvalidUUIDException(@NotNull CommandParameter parameter, @NotNull String input, @NotNull CommandActor actor) {
        super(parameter, input, actor);
    }
}

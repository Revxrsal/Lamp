package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;

/**
 * Thrown when an invalid value is specified for an {@link Enum} parameter.
 */
public class EnumNotFoundException extends InvalidValueException {

    public EnumNotFoundException(@NotNull CommandParameter parameter, @NotNull String input, @NotNull CommandActor actor) {
        super(parameter, input, actor);
    }
}

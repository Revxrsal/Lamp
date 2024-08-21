package revxrsal.commands.sponge.exception;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.world.World;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when an invalid value for a {@link World} parameter
 * is inputted in the command
 */
public class InvalidWorldException extends InvalidValueException {

    public InvalidWorldException(@NotNull String input) {
        super(input);
    }
}

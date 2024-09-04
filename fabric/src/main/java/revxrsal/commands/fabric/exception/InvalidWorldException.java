package revxrsal.commands.fabric.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when an invalid value for a {@link net.minecraft.world.World} parameter
 * is inputted in the command
 */
public class InvalidWorldException extends InvalidValueException {

    public InvalidWorldException(@NotNull String input) {
        super(input);
    }
}

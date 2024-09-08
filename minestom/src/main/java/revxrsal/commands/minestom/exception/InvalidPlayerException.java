package revxrsal.commands.minestom.exception;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when an invalid value for a {@link Player} parameter
 * is inputted in the command
 */
public class InvalidPlayerException extends InvalidValueException {

    public InvalidPlayerException(@NotNull String input) {
        super(input);
    }
}

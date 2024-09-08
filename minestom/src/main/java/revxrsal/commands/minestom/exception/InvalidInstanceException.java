package revxrsal.commands.minestom.exception;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when an invalid value for a {@link Instance} parameter
 * is inputted in the command
 */
public class InvalidInstanceException extends InvalidValueException {

    public InvalidInstanceException(@NotNull String input) {
        super(input);
    }
}

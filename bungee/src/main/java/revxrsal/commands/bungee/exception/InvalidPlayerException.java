package revxrsal.commands.bungee.exception;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when an invalid value for a {@link ProxiedPlayer} parameter
 * is inputted in the command
 */
public class InvalidPlayerException extends InvalidValueException {

    public InvalidPlayerException(@NotNull String input) {
        super(input);
    }
}

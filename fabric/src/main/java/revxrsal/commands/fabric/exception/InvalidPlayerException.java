package revxrsal.commands.fabric.exception;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when an invalid value for a {@link ServerPlayerEntity} parameter
 * is inputted in the command
 */
public class InvalidPlayerException extends InvalidValueException {

    public InvalidPlayerException(@NotNull String input) {
        super(input);
    }
}

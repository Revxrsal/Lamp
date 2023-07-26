package revxrsal.commands.sponge.exception;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when an invalid value for a {@link ServerPlayer}
 * parameter is inputted in the command
 */
public class InvalidPlayerException extends InvalidValueException {

    public InvalidPlayerException(@NotNull CommandParameter parameter, @NotNull String input) {
        super(parameter, input);
    }
}

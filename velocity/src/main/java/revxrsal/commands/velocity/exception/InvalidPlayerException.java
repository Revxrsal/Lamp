package revxrsal.commands.velocity.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;
import revxrsal.commands.velocity.VelocityCommandActor;

/**
 * Thrown when an invalid value for a {@link com.velocitypowered.api.proxy.Player}
 * parameter is inputted in the command
 */
public class InvalidPlayerException extends InvalidValueException {

    public InvalidPlayerException(@NotNull CommandParameter parameter, @NotNull String input, @NotNull CommandActor actor) {
        super(parameter, input, actor);
    }

    @Override public @NotNull VelocityCommandActor getActor() {
        return super.getActor().as(VelocityCommandActor.class);
    }
}

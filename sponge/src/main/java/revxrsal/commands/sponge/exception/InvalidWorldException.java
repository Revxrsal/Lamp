package revxrsal.commands.sponge.exception;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.world.World;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;
import revxrsal.commands.sponge.SpongeCommandActor;

/**
 * Thrown when an invalid value for a {@link World} parameter
 * is inputted in the command
 */
public class InvalidWorldException extends InvalidValueException {

    public InvalidWorldException(@NotNull CommandParameter parameter, @NotNull String input, @NotNull CommandActor actor) {
        super(parameter, input, actor);
    }

    @Override public @NotNull SpongeCommandActor getActor() {
        return super.getActor().as(SpongeCommandActor.class);
    }
}

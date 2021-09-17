package revxrsal.commands.bukkit.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when an invalid value for a {@link org.bukkit.World} parameter
 * is inputted in the command
 */
public class InvalidWorldException extends InvalidValueException {

    public InvalidWorldException(@NotNull CommandParameter parameter, @NotNull String input, @NotNull CommandActor actor) {
        super(parameter, input, actor);
    }

    @Override public @NotNull BukkitCommandActor getActor() {
        return super.getActor().as(BukkitCommandActor.class);
    }
}

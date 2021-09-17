package revxrsal.commands.bungee.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when an invalid value for a {@link net.md_5.bungee.api.connection.ProxiedPlayer}
 * parameter is inputted in the command
 */
public class InvalidPlayerException extends InvalidValueException {

    public InvalidPlayerException(@NotNull CommandParameter parameter, @NotNull String input, @NotNull CommandActor actor) {
        super(parameter, input, actor);
    }

    @Override public @NotNull BungeeCommandActor getActor() {
        return super.getActor().as(BungeeCommandActor.class);
    }
}

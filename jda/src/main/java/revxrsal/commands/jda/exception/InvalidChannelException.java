package revxrsal.commands.jda.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;

public class InvalidChannelException extends InvalidValueException {

    public InvalidChannelException(@NotNull CommandParameter parameter, @NotNull String input) {
        super(parameter, input);
    }
}

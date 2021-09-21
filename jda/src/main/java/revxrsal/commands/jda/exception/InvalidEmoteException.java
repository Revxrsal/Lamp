package revxrsal.commands.jda.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;

public class InvalidEmoteException extends InvalidValueException {

    public InvalidEmoteException(@NotNull CommandParameter parameter, @NotNull String input) {
        super(parameter, input);
    }
}

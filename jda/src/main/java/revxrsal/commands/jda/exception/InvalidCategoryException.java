package revxrsal.commands.jda.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;

public class InvalidCategoryException extends InvalidValueException {

    public InvalidCategoryException(@NotNull CommandParameter parameter, @NotNull String input) {
        super(parameter, input);
    }
}

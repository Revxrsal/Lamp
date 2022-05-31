package revxrsal.commands.bukkit.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when an invalid value for a {@link org.bukkit.NamespacedKey} parameter is inputted in the command
 */
public class InvalidNamespacedKeyException extends InvalidValueException {

    public InvalidNamespacedKeyException(@NotNull CommandParameter parameter, @NotNull String input) {
        super(parameter, input);
    }
}

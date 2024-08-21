package revxrsal.commands.sponge.exception;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.selector.Selector;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when a malformed {@link Selector} is inputted for a command.
 */
public class MalformedSelectorException extends InvalidValueException {

    /**
     * The syntax error message
     */
    private final @NotNull String errorMessage;

    public MalformedSelectorException(@NotNull String input, @NotNull String errorMessage) {
        super(input);
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the error message produced by the native parser
     *
     * @return The error message
     */
    public @NotNull String errorMessage() {
        return errorMessage;
    }
}

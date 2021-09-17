package revxrsal.commands.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.help.CommandHelp;

/**
 * Thrown when an invalid page is supplied in {@link CommandHelp#paginate(int, int)}.
 */
@Getter
@AllArgsConstructor
public class InvalidHelpPageException extends RuntimeException {

    /**
     * The command help entries list
     */
    private final @NotNull CommandHelp<?> commandHelp;

    /**
     * The invalid page number
     */
    private final int page;

}

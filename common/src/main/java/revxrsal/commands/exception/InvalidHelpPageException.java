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
@ThrowableFromCommand
public class InvalidHelpPageException extends RuntimeException {

    /**
     * The command help entries list
     */
    private final @NotNull CommandHelp<?> commandHelp;

    /**
     * The invalid page number
     */
    private final int page;

    /**
     * The number of entries in each page
     */
    private final int elementsPerPage;

    /**
     * Returns the number of available pages in this command help.
     *
     * @return The page count
     */
    public int getPageCount() {
        return commandHelp.getPageSize(elementsPerPage);
    }

}

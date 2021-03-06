package revxrsal.commands.help;

import org.jetbrains.annotations.Range;
import revxrsal.commands.exception.InvalidHelpPageException;

import java.util.List;

/**
 * Represents an iterable of the entries generated by the {@link CommandHelpWriter}.
 * <p>
 * This class contains useful methods to allow paginating the help entries.
 *
 * @param <T> The help entry type. See {@link CommandHelpWriter} for more information.
 */
public interface CommandHelp<T> extends List<T> {

    /**
     * Returns a {@link CommandHelp} that contains the help entries
     * in a paginated manner
     *
     * @param page            Page to include elements of
     * @param elementsPerPage The elements to include in each page
     * @return The new command help
     */
    CommandHelp<T> paginate(int page, int elementsPerPage) throws InvalidHelpPageException;

    /**
     * Returns the number of pages that would be generated when
     * including x elements per page.
     *
     * @param elementsPerPage Elements to include in each page. Must be at least 1
     * @return The page size
     * @throws IllegalArgumentException if elementsPerPage is less than 1.
     */
    @Range(from = 1, to = Long.MAX_VALUE)
    int getPageSize(int elementsPerPage);

}

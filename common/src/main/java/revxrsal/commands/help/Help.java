/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.help;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.InvalidHelpPageException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains useful interfaces for generating help commands
 */
public interface Help {

    /**
     * Returns the list of commands that belong to a specific page after paginating
     * this list.
     * <p>
     * Note that the list returned by this method is immutable.
     *
     * @param commands        The list of commands to paginate
     * @param page            The page number
     * @param elementsPerPage The elements to include in each page
     * @return The pages list.
     * @throws InvalidHelpPageException if {@code pageNumber} is greater than
     *                                  {@link #numberOfPages(int, int)} or less than 1
     */
    static <A extends CommandActor> @NotNull @Unmodifiable List<ExecutableCommand<A>> paginate(
            @NotNull List<ExecutableCommand<A>> commands,
            @Range(from = 1, to = Integer.MAX_VALUE) int page,
            @Range(from = 1, to = Integer.MAX_VALUE) int elementsPerPage
    ) throws InvalidHelpPageException {
        if (commands.isEmpty())
            return List.of();
        int size = numberOfPages(commands.size(), elementsPerPage);
        if (page <= 0)
            throw new InvalidHelpPageException(commands, page, elementsPerPage, size);
        List<ExecutableCommand<A>> list = new ArrayList<>();
        if (page > size)
            throw new InvalidHelpPageException(commands, page, elementsPerPage, size);
        int listIndex = page - 1;
        int l = Math.min(page * elementsPerPage, commands.size());
        for (int i = listIndex * elementsPerPage; i < l; ++i) {
            list.add(commands.get(i));
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns the number of pages this list would generate if
     * it were to be split pages where each page contains {@code elementsPerPage}
     * elements.
     *
     * @param numberOfEntries The number of entries
     * @param elementsPerPage Maximum number of elements to include in each page. Note
     *                        that the last page may contain less than this number (it will
     *                        be the remainder)
     * @return The number of pages
     */
    static @Range(from = 1, to = Long.MAX_VALUE) int numberOfPages(
            @Range(from = 0, to = Integer.MAX_VALUE) int numberOfEntries,
            @Range(from = 1, to = Integer.MAX_VALUE) int elementsPerPage
    ) {
        if (elementsPerPage < 1)
            throw new IllegalArgumentException("Elements per page cannot be less than 1! (Found " + elementsPerPage + ")");
        return (numberOfEntries / elementsPerPage) + (numberOfEntries % elementsPerPage == 0 ? 0 : 1);
    }

    /**
     * Represents a generic list of {@link ExecutableCommand ExecutableCommands} that can be
     * split into pages
     *
     * @param <A> The actor type
     */
    interface CommandList<A extends CommandActor> extends Iterable<ExecutableCommand<A>> {

        /**
         * Returns <em>all</em> the commands that belong to this list. This does
         * not respect pages.
         * <p>
         * Note that the returned list is immutable.
         *
         * @return All the commands
         */
        @Unmodifiable
        List<ExecutableCommand<A>> all();

        /**
         * Returns the number of pages this list would generate if
         * it were to be split pages where each page contains {@code elementsPerPage}
         * elements.
         *
         * @param elementsPerPage Maximum number of elements to include in each page. Note
         *                        that the last page may contain less than this number (it will
         *                        be the remainder)
         * @return The number of pages
         */
        @Range(from = 1, to = Integer.MAX_VALUE)
        int numberOfPages(@Range(from = 1, to = Integer.MAX_VALUE) int elementsPerPage);

        /**
         * Returns the list of commands that belong to a specific page after paginating
         * this list.
         * <p>
         * Note that the list returned by this method is immutable.
         *
         * @param pageNumber      The page number
         * @param elementsPerPage The elements to include in each page
         * @return The pages list.
         * @throws InvalidHelpPageException if {@code pageNumber} is greater than
         *                                  {@link #numberOfPages(int)} or less than 1
         * @deprecated Use {@link #paginate(int, int)} instead.
         */
        @Unmodifiable
        @Deprecated(forRemoval = true)
        default List<ExecutableCommand<A>> asPage(
                @Range(from = 1, to = Integer.MAX_VALUE) int pageNumber,
                @Range(from = 1, to = Integer.MAX_VALUE) int elementsPerPage
        ) throws InvalidHelpPageException {
            return paginate(pageNumber, elementsPerPage);
        }

        /**
         * Returns the list of commands that belong to a specific page after paginating
         * this list.
         * <p>
         * Note that the list returned by this method is immutable.
         *
         * @param pageNumber      The page number
         * @param elementsPerPage The elements to include in each page
         * @return The pages list.
         * @throws InvalidHelpPageException if {@code pageNumber} is greater than
         *                                  {@link #numberOfPages(int)} or less than 1
         */
        @Unmodifiable
        List<ExecutableCommand<A>> paginate(
                @Range(from = 1, to = Integer.MAX_VALUE) int pageNumber,
                @Range(from = 1, to = Integer.MAX_VALUE) int elementsPerPage
        ) throws InvalidHelpPageException;
    }

    /**
     * Contains all children commands of a certain {@link ExecutableCommand}.
     *
     * @param <A> The actor type
     * @see ExecutableCommand#childrenCommands()
     */
    interface ChildrenCommands<A extends CommandActor> extends CommandList<A> {}

    /**
     * Contains all sibling commands of a certain {@link ExecutableCommand}. This
     * is all commands that are on the exact same level as this command.
     *
     * @param <A> The actor type
     * @see ExecutableCommand#siblingCommands()
     */
    interface SiblingCommands<A extends CommandActor> extends CommandList<A> {}

    /**
     * Contains all children <em>and</em> sibling commands of a certain {@link ExecutableCommand}.
     *
     * @param <A> The actor type
     * @see ExecutableCommand#relatedCommands()
     */
    interface RelatedCommands<A extends CommandActor> extends CommandList<A> {}

}

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
package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.help.Help.CommandList;

import java.util.List;

/**
 * Thrown when an invalid page is passed to {@link CommandList#paginate(int, int)}
 */
@ThrowableFromCommand
public class InvalidHelpPageException extends RuntimeException {

    private final List<ExecutableCommand<CommandActor>> commands;
    private final int page;
    private final int elementsPerPage;
    private final int numberOfPages;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <A extends CommandActor> InvalidHelpPageException(List<ExecutableCommand<A>> commands, int page, int elementsPerPage, int numberOfPages) {
        this.commands = (List) commands;
        this.page = page;
        this.elementsPerPage = elementsPerPage;
        this.numberOfPages = numberOfPages;
    }

    /**
     * Returns the elements to include in each page
     *
     * @return The elements to include in each page
     */
    public int elementsPerPage() {
        return elementsPerPage;
    }

    /**
     * Returns  the page number
     *
     * @return The page number
     */
    public int page() {
        return page;
    }

    /**
     * Returns the number of pages this list would generate if
     * it were to be split pages where each page contains {@link #elementsPerPage()}
     * elements.
     *
     * @return The number of pages
     */
    public @Range(from = 1, to = Integer.MAX_VALUE) int numberOfPages() {
        return numberOfPages;
    }

    /**
     * Returns <em>all</em> the commands that belong to this list. This does
     * not respect pages.
     * <p>
     * Note that the returned list is immutable.
     *
     * @return All the commands
     */
    // blame java for not allowing generics in exceptions
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <A extends CommandActor> @NotNull @Unmodifiable List<ExecutableCommand<A>> commands() {
        return (List) commands;
    }
}

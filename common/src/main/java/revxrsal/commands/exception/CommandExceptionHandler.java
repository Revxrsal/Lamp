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
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.context.ErrorContext;

/**
 * A handler for all exceptions that may be thrown during the command
 * invocation.
 * <p>
 * Exceptions may be anything, including Java's normal exceptions,
 * and build-in ones thrown by different components in the framework.
 * <p>
 * Set with {@link Lamp.Builder#exceptionHandler(CommandExceptionHandler)}.
 * <p>
 * For a flexible yet powerful implementation, see {@link RuntimeExceptionAdapter}
 * which allows handling exceptions in smaller functions
 *
 * @see RuntimeExceptionAdapter
 */
@FunctionalInterface
public interface CommandExceptionHandler<A extends CommandActor> {

    /**
     * Handles the given exception.
     *
     * @param throwable    Exception to handle
     * @param errorContext The context in which the error occurred. For example,
     *                     if the error was because a parameter was invalid, this
     *                     will be a {@link ErrorContext.ParsingParameter} context,
     *                     which contains information about the parameter being parsed.
     */
    void handleException(@NotNull Throwable throwable, @NotNull ErrorContext<A> errorContext);

}

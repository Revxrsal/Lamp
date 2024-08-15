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
package revxrsal.commands.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.exception.context.ErrorContext;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.stream.MutableStringStream;

/**
 * Represents the result obtained from attempting to parse an {@link ExecutableCommand}
 * with some {@link MutableStringStream} input.
 * <p>
 * This attempt may either be successful, or failed with an error ({@link Throwable}).
 * <p>
 * Note that this will send no feedback at all to the user. It can continue execution
 * with {@link #execute()} or handle the exception with {@link #handleException()}.
 *
 * @param <A> The actor type
 * @see ExecutableCommand#test(CommandActor, MutableStringStream)
 */
public interface Potential<A extends CommandActor> extends Comparable<Potential<A>> {

    /**
     * Tests whether the execution was successful or not
     *
     * @return if the execution was successful or not
     */
    boolean successful();

    /**
     * Tests whether the execution failed or not
     *
     * @return if the execution failed or not
     */
    boolean failed();

    /**
     * Returns the {@link ExecutionContext} generated from the execution
     * attempt, as well as any values parsed from the input.
     *
     * @return The execution context
     */
    @NotNull ExecutionContext<A> context();

    /**
     * Returns the error generated from the execution attempt. If
     * the attempt was {@link #successful()}, this will be {@code null}.
     *
     * @return The error
     */
    @Nullable Throwable error();

    /**
     * Returns the context in which the error has occurred.
     * <p>
     * For example, if the error was because a parameter was invalid, this
     * will be a {@link ErrorContext.ParsingParameter} context,
     * which contains information about the parameter being parsed.
     * <p>
     * If, however, it occurred during executing the command function,
     * this will be an {@link ErrorContext.ExecutingFunction} context.
     *
     * @return The error context, or {@code null} if this was
     * successful
     */
    @Nullable ErrorContext<A> errorContext();

    /**
     * Handles the exception that caused the attempt to fail.
     * <p>
     * If the attempt was successful, this method will do nothing
     */
    void handleException();

    /**
     * Executes the command.
     * <p>
     * If the attempt was not successful, this method will do nothing
     */
    void execute();

}

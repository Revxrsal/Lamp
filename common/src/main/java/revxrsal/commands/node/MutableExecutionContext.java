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
package revxrsal.commands.node;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;

/**
 * Represents the context of the execution of the command. This
 * is a mutable variant of {@link ExecutionContext} as it allows modifying
 * the arguments that have been resolved.
 *
 * @param <A> The actor type
 */
public interface MutableExecutionContext<A extends CommandActor> extends ExecutionContext<A> {

    /**
     * Adds the given parameter name to this context
     *
     * @param name  The parameter name
     * @param value The parameter value
     */
    void addResolvedArgument(@NotNull String name, Object value);

    /**
     * Clears all the resolved arguments. Useful for reusing the same context
     * for multiple commands
     */
    void clearResolvedArguments();
}

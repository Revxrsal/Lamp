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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the context of the execution of the command.
 *
 * @param <A> The actor type
 */
public interface ExecutionContext<A extends CommandActor> {

    /**
     * Returns the actor executing this command
     *
     * @return The actor
     */
    @NotNull A actor();

    /**
     * Returns the {@link Lamp} instance that is executing this
     * command
     *
     * @return The {@link Lamp} instance
     */
    @NotNull Lamp<A> lamp();

    /**
     * Returns the command being executed
     *
     * @return The executed command
     */
    @NotNull ExecutableCommand<A> command();

    /**
     * Returns the arguments that have been resolved, each mapped to the
     * parameter name.
     *
     * @return The arguments that have been resolved
     */
    @NotNull
    @UnmodifiableView
    Map<String, Object> resolvedArguments();

    /**
     * Returns the argument that has been resolved with the given name
     *
     * @param argumentName The argument name
     * @param <T>          The parameter type
     * @return The parameter, or null if it was not resolved
     */
    @Nullable
    <T> T getResolvedArgumentOrNull(@NotNull String argumentName);

    /**
     * Returns the first argument that matches the given type
     *
     * @param argumentType The argument type
     * @param <T>          The parameter type
     * @return The parameter, or null if it was not resolved
     */
    @Nullable
    <T> T getResolvedArgumentOrNull(@NotNull Class<T> argumentType);

    /**
     * Returns the first argument that matches the given type, or throws
     * an {@link IllegalArgumentException} if not found.
     *
     * @param argumentName The argument name
     * @param <T>          The parameter type
     * @return The parameter
     * @throws IllegalArgumentException if no such parameter found
     */
    default @NotNull <T> T getResolvedArgument(@NotNull String argumentName) {
        T argument = getResolvedArgumentOrNull(argumentName);
        if (argument == null)
            throw new IllegalArgumentException("Argument '" + argumentName + "' not found (or hasn't been resolved yet). " +
                    "Possible argument names: " + resolvedArguments().keySet());
        return argument;
    }

    /**
     * Returns the first argument that matches the given type, or throws
     * an {@link IllegalArgumentException} if not found.
     *
     * @param argumentType The argument type
     * @param <T>          The parameter type
     * @return The parameter
     * @throws IllegalArgumentException if no such parameter found
     */
    default @NotNull <T> T getResolvedArgument(@NotNull Class<T> argumentType) {
        T argument = getResolvedArgumentOrNull(argumentType);
        if (argument == null) {
            List<Class<?>> types = resolvedArguments().values().stream()
                    .filter(Objects::nonNull)
                    .map(Object::getClass)
                    .collect(Collectors.toList());
            throw new IllegalArgumentException("Couldn't find an argument that matches the type " + argumentType + ". Available types: " + types);
        }
        return argument;
    }
}

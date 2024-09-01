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
package revxrsal.commands.parameter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.ParseWith;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.parameter.builtins.ClassParameterTypeFactory;
import revxrsal.commands.stream.MutableStringStream;

import java.lang.reflect.Type;

/**
 * Represents a parameter type that produces values from the user input {@link MutableStringStream}.
 * <p>
 * A parameter type is allowed to consume as much of the {@link MutableStringStream} as needed,
 * however, it may <em>not</em> consume parts of strings. If it needs to consume a
 * single string, it must consume it entirely.
 * <p>
 * Note that Lamp will automatically register resolvers for the following types:
 * <ul>
 *     <li>{@code List<T>} for any parameter type T</li>
 *     <li>{@code Set<T>} for any parameter type T</li>
 *     <li>{@code T[]} for any parameter type T</li>
 *     <li>Enum types</li>
 *     <li>Primitive types ({@code int}s, {@code double}s, {@code boolean}s, {@code char}s, etc.)</li>
 *     <li>{@link String} type</li>
 * </ul>
 * A parameter may request to be parsed with a specific parameter type
 * using the {@link ParseWith @ParseWith} annotation.
 *
 * @param <A> The actor type
 * @param <T> The parameter type
 * @see ParameterTypes
 */
@FunctionalInterface
public interface ParameterType<A extends CommandActor, T> extends BaseParameterType {

    /**
     * Reads input from the given {@link MutableStringStream}, parses the object, or throws
     * exceptions if needed.
     *
     * @param input   The input stream. This argument type is free to consume as much as it needs
     * @param context The command execution context, as well as arguments that have been resolved
     * @return The parsed object. This should never be null.
     */
    T parse(
            @NotNull MutableStringStream input,
            @NotNull ExecutionContext<@NotNull A> context
    );

    /**
     * Returns the default suggestions. These will be sent if the
     * containing {@link ParameterNode} does not
     * supply any custom suggestions.
     *
     * @return The default type suggestions
     */
    @NotNull default SuggestionProvider<@NotNull A> defaultSuggestions() {
        return SuggestionProvider.empty();
    }

    /**
     * The priority for an argument of this type over other candidate arguments.
     * <p>
     * An example of this, is when a command has the following signatures:
     * <ul>
     *     <li>/foo [integer]</li>
     *     <li>/foo [string]</li>
     * </ul>
     * <p>
     * Because an integer parameter can only accept a certain type of input (i.e. numbers),
     * it will have higher priority over a string parameter, and as such, if the user
     * inputs {@code /foo 10}, the integer will be tested first, before moving on to the
     * string.
     * <p>
     * For more details on the priority API, see {@link PrioritySpec}
     *
     * @return the parse priority for this parameter
     * @see PrioritySpec
     */
    default @NotNull PrioritySpec parsePriority() {
        return PrioritySpec.defaultPriority();
    }

    /**
     * Returns whether this parameter is greedy or not. Greedy parameters
     * are treated slightly differently:
     * <ul>
     *     <li>They must come at the end of the command</li>
     *     <li>They can provide tab completes indefinitely</li>
     * </ul>
     *
     * @return if this parameter is greedy or not
     */
    default boolean isGreedy() {
        return false;
    }

    /**
     * Represents a factory that constructs {@link ParameterType}s dynamically
     *
     * @param <A> The command actor type
     */
    interface Factory<A extends CommandActor> extends ParameterFactory, BaseParameterType {

        /**
         * Creates a new {@link Factory} that returns a {@link ParameterType} for all
         * parameters that <em>exactly</em> have a certain class.
         *
         * @param type          Parameter type to check against
         * @param parameterType The parameter type to supply
         * @param <A>           The actor type
         * @param <T>           The parameter type
         * @return The parameter type factory
         */
        static <A extends CommandActor, T> @NotNull Factory<A> forType(@NotNull Class<T> type, @NotNull ParameterType<A, T> parameterType) {
            return new ClassParameterTypeFactory<>(type, parameterType, false);
        }

        /**
         * Creates a new {@link Factory} that returns a {@link ParameterType} for all
         * parameters that are assignable from the given {@code type}.
         * <p>
         * Checking is done using {@link Class#isAssignableFrom(Class)}.
         *
         * @param type          Parameter type to check against
         * @param parameterType The parameter type to supply
         * @param <A>           The actor type
         * @param <T>           The parameter type
         * @return The parameter type factory
         */
        static <A extends CommandActor, T> @NotNull Factory<A> forTypeAndSubclasses(@NotNull Class<T> type, @NotNull ParameterType<A, T> parameterType) {
            return new ClassParameterTypeFactory<>(type, parameterType, true);
        }

        /**
         * Dynamically creates a {@link ParameterType}
         *
         * @param <T>           The parameter type
         * @param parameterType The command parameter to create for
         * @param annotations   The parameter annotations
         * @param lamp          The Lamp instance (for referencing other parameter types)
         * @return The newly created {@link ParameterType}, or {@code null} if this factory
         * cannot deal with it.
         */
        @Nullable
        <T> ParameterType<A, T> create(@NotNull Type parameterType, @NotNull AnnotationList annotations, @NotNull Lamp<A> lamp);

    }
}

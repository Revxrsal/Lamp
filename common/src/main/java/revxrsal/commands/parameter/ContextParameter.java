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
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.builtins.ClassContextParameterFactory;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.lang.reflect.Type;

/**
 * Represents a parameter that is resolved from the context of the
 * command.
 *
 * @param <A> The actor type
 * @param <T> The parameter type
 */
@FunctionalInterface
public interface ContextParameter<A extends CommandActor, T> {

    /**
     * Reads input from the given {@link MutableStringStream}, parses the object, or throws
     * exceptions if needed.
     *
     * @param parameter The parameter
     * @param input     The input stream. This argument type is free to consume as much as it needs
     * @param context   The command execution context, as well as arguments that have been resolved
     * @return The parsed object. This should never be null.
     */
    T resolve(
            @NotNull CommandParameter parameter,
            @NotNull StringStream input,
            @NotNull ExecutionContext<A> context
    );

    /**
     * Represents a factory that constructs {@link ContextParameter}s dynamically
     *
     * @param <A> The command actor type
     */
    interface Factory<A extends CommandActor> extends ParameterFactory {

        /**
         * Creates a new {@link Factory} that returns a {@link ContextParameter} for all
         * parameters that <em>exactly</em> have a certain class.
         *
         * @param type          Parameter type to check against
         * @param parameterType The parameter type to supply
         * @param <A>           The actor type
         * @param <T>           The parameter type
         * @return The parameter type factory
         */
        static @NotNull <A extends CommandActor, T> Factory<A> forType(@NotNull Class<T> type, @NotNull ContextParameter<A, T> parameterType) {
            return new ClassContextParameterFactory<>(type, parameterType, false);
        }

        /**
         * Creates a new {@link Factory} that returns a {@link ContextParameter} for all
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
        static <A extends CommandActor, T> @NotNull Factory<A> forTypeAndSubclasses(@NotNull Class<T> type, @NotNull ContextParameter<A, T> parameterType) {
            return new ClassContextParameterFactory<>(type, parameterType, true);
        }

        /**
         * Dynamically creates a {@link ContextParameter}
         *
         * @param <T>           The parameter type
         * @param parameterType The command parameter to create for
         * @param annotations   The parameter annotations
         * @param lamp          The Lamp instance (for referencing other parameter types)
         * @return The newly created {@link ContextParameter}, or {@code null} if this factory
         * cannot deal with it.
         */
        @Nullable
        <T> ContextParameter<A, T> create(@NotNull Type parameterType, @NotNull AnnotationList annotations, @NotNull Lamp<A> lamp);
    }
}

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
package revxrsal.commands.brigadier.types;

import com.mojang.brigadier.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ParameterNode;

import static revxrsal.commands.util.Classes.wrap;

/**
 * A resolver that creates dedicated {@link ArgumentType}s for parameters. This
 * can read annotations and other information to construct a suitable argument
 * type.
 * <p>
 * Register with {@link ArgumentTypes.Builder#addTypeFactory(ArgumentTypeFactory)}.
 */
@FunctionalInterface
public interface ArgumentTypeFactory<A extends CommandActor> {

    /**
     * Returns the argument type for the given parameter. If this resolver
     * cannot deal with the parameter, it may return null.
     *
     * @param parameter Parameter to create for
     * @return The argument type
     */
    @Nullable ArgumentType<?> getArgumentType(@NotNull ParameterNode<A, ?> parameter);

    /**
     * Creates a {@link ArgumentTypeFactory} that will return the same
     * argument type for all parameters that match a specific type
     *
     * @param type         Type to check for
     * @param argumentType The argument type to return
     * @return The argument type factory
     */
    static @NotNull <A extends CommandActor> ArgumentTypeFactory<A> forType(Class<?> type, ArgumentType<?> argumentType) {
        Class<?> wrapped = wrap(type);
        return parameter -> wrap(parameter.type()) == wrapped ? argumentType : null;
    }

    /**
     * Creates a {@link ArgumentTypeFactory} that will return the same
     * argument type for all parameters that match a specific type and
     * all of its subclasses.
     *
     * @param type         Type to check for
     * @param argumentType The argument type to return
     * @return The argument type factory
     */
    static @NotNull <A extends CommandActor> ArgumentTypeFactory<A> forTypeAndSubclasses(Class<?> type, ArgumentType<?> argumentType) {
        Class<?> wrapped = wrap(type);
        return parameter -> wrapped.isAssignableFrom(wrap(parameter.type())) ? argumentType : null;
    }
}

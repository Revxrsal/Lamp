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
package revxrsal.commands.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.util.ClassMap;

/**
 * Represents a convenient way to hook into Brigadier and respect
 * minor differences in different implementations
 */
public interface LampBrigadier {

    /**
     * Wraps Brigadier's command sender with the platform's appropriate {@link CommandActor}
     *
     * @param commandSource Source to wrap
     * @return The wrapped command source
     */
    @NotNull CommandActor wrapSource(@NotNull Object commandSource);

    /**
     * Registers the given command node to the dispatcher.
     *
     * @param node Node to register
     */
    void register(@NotNull LiteralCommandNode<?> node);

    /**
     * Returns the {@link ArgumentType} corresponding to this parameter. This
     * may be used to return certain argument types that cannot be registered
     * in {@link #getAdditionalArgumentTypes()} (for example, generic types).
     *
     * @param parameter Parameter to get for
     * @return The argument type, or {@code null} if not applicable.
     */
    @Nullable default ArgumentType<?> getArgumentType(@NotNull CommandParameter parameter) {return null;}

    /**
     * Returns the special argument types for parameter types.
     *
     * @return Additional argument types
     */
    @NotNull ClassMap<ArgumentType<?>> getAdditionalArgumentTypes();

    /**
     * Registers the given command node builder to the dispatcher
     *
     * @param node Node to register
     */
    default void register(@NotNull LiteralArgumentBuilder<?> node) {
        register(node.build());
    }
}

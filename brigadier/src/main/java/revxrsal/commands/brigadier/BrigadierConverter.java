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
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ParameterNode;

/**
 * Represents an intermediate layer that allows for simple interoperability
 * with {@link BrigadierAdapter}.
 *
 * @param <A> The Lamp actor type
 * @param <S> The Brigadier actor type
 */
public interface BrigadierConverter<A extends CommandActor, S> {

    /**
     * Returns the argument type that corresponds to the given {@link ParameterNode}.
     * <p>
     * In general, adapters should maintain an instance of {@link ArgumentTypes}
     * and delegate the logic of this function to {@link ArgumentTypes#type(ParameterNode)},
     * which is guaranteed to return the correct results
     *
     * @param parameter The parameter to get for
     * @return The argument type
     */
    @NotNull ArgumentType<?> getArgumentType(@NotNull ParameterNode<A, ?> parameter);

    /**
     * Wraps the given Brigadier sender to a Lamp {@link CommandActor}
     *
     * @param sender Sender to wrap
     * @param lamp   The Lamp instance
     * @return The wrapped actor
     */
    @NotNull A createActor(@NotNull S sender, @NotNull Lamp<A> lamp);

}

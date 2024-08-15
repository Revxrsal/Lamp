/*
 * This file is part of sweeper, licensed under the MIT License.
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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;

/**
 * Represents a literal node, i.e. it has only a single possible value
 * which is {@link #name()}.
 *
 * @param <A> The actor type
 */
public interface LiteralNode<A extends CommandActor> extends CommandNode<A> {

    /**
     * Tests whether this node is a {@link LiteralNode}. This always
     * returns true
     *
     * @return if this node is literal
     */
    @Override
    default boolean isLiteral() {
        return true;
    }

    /**
     * Requires this node to be a {@link LiteralNode} and automatically
     * casts it
     *
     * @return This node, as a literal node
     */
    @Override
    @Contract(value = "-> this", pure = true)
    default @NotNull LiteralNode<A> requireLiteralNode() {
        return this;
    }

    /**
     * Tests whether this node is a {@link ParameterNode}. This
     * always returns false
     *
     * @return if this node is a parameter
     */
    @Override
    default boolean isParameter() {
        return false;
    }

    /**
     * Requires this node to be a {@link ParameterNode}. This will throw
     * a {@link IllegalStateException}.
     *
     * @return never as it always fails
     * @throws IllegalStateException always
     */
    @Override
    @Contract("-> fail")
    default <T> @NotNull ParameterNode<A, T> requireParameterNode() {
        throw new IllegalStateException("Expected a ParameterNode, found a LiteralNode");
    }
}

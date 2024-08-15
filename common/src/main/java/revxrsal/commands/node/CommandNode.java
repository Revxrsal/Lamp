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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.stream.MutableStringStream;

/**
 * Represents a node in a command tree. This can either be a {@link LiteralNode}
 * or a {@link ParameterNode}.
 *
 * @param <A> The actor type. This allows for better type inference and
 *            reduces the need for type-checking and casting
 * @see LiteralNode
 * @see ParameterNode
 */
public interface CommandNode<A extends CommandActor> extends Comparable<CommandNode<A>> {

    /**
     * Returns the name of this node.
     *
     * @return The node name
     */
    @NotNull String name();

    /**
     * Returns the action bound to this command node.
     *
     * @return The action of this node, or {@code null} if none
     * is registered.
     */
    @Nullable CommandAction<A> action();

    /**
     * Tests whether is this node the last one in the command tree
     * it belongs to
     *
     * @return if the node is the last in the tree.
     */
    boolean isLast();

    /**
     * Tests whether this node has an {@link #action()}.
     *
     * @return if this node has an action or not
     */
    default boolean hasAction() {
        return action() != null;
    }

    /**
     * Executes the {@link #action()} of this node with the given context and input.
     * <p>
     * If this node has no action, this method does nothing.
     *
     * @param context The execution context
     * @param input   The user input
     */
    void execute(@NotNull ExecutionContext<A> context, @NotNull MutableStringStream input);

    /**
     * Tests whether this node is a {@link LiteralNode}
     *
     * @return if this node is literal
     */
    boolean isLiteral();

    /**
     * Requires this node to be a {@link LiteralNode} and automatically
     * casts it, otherwise throws a {@link IllegalStateException}.
     *
     * @return This node, as a literal node
     * @throws IllegalStateException if this is not a literal node
     */
    @NotNull LiteralNode<A> requireLiteralNode();

    /**
     * Tests whether this node is a {@link ParameterNode}
     *
     * @return if this node is a parameter
     */
    boolean isParameter();

    /**
     * Requires this node to be a {@link ParameterNode} and automatically
     * casts it, otherwise throws a {@link IllegalStateException}.
     *
     * @return This node, as a parameter node
     * @throws IllegalStateException if this is not a parameter node
     */
    @NotNull <T> ParameterNode<A, T> requireParameterNode();

}

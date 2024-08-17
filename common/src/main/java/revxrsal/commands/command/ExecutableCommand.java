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

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.SecretCommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.node.CommandNode;
import revxrsal.commands.node.HasDescription;
import revxrsal.commands.node.LiteralNode;
import revxrsal.commands.node.RequiresPermission;
import revxrsal.commands.stream.MutableStringStream;

import java.util.List;

/**
 * Represents an immutable, full, separate command path that may be executed.
 *
 * @param <A> The actor type
 * @see #test(CommandActor, MutableStringStream)
 */
public interface ExecutableCommand<A extends CommandActor> extends Comparable<ExecutableCommand<A>>, Iterable<CommandNode<A>>, RequiresPermission<A>, HasDescription {

    /**
     * Returns the {@link Lamp} instance that created this command
     *
     * @return The lamp instance
     */
    @NotNull Lamp<A> lamp();

    /**
     * Returns the amount of nodes in this command. Larger nodes
     * have higher priority over others.
     *
     * @return The command node
     */
    int size();

    /**
     * Returns the number of optional parameters in this command
     *
     * @return The number of optional parameters
     */
    int optionalParameters();

    /**
     * Returns the number of required literals or parameters in this command
     *
     * @return The number of required literals or parameters
     */
    int requiredInput();

    /**
     * Returns the path of this command. This will include
     * parameters enclosed by brackets
     *
     * @return The path of the command.
     */
    @NotNull
    String path();

    /**
     * Returns the usage of this command. This can be explicitly set
     * with {@link Usage @Usage}. Otherwise, it will be auto-generated
     *
     * @return The command usage
     */
    @NotNull
    String usage();

    /**
     * Returns the description of this command. This can be explicitly set
     * with {@link Description @Description}.
     *
     * @return The description, or null if none is set.
     */
    @Nullable
    String description();

    /**
     * Returns the underlying {@link CommandFunction} of this
     * command
     *
     * @return The underlying function
     */
    @NotNull
    CommandFunction function();

    /**
     * Returns the very last node in this command.
     *
     * @return The last node
     */
    @NotNull
    CommandNode<A> lastNode();

    /**
     * Returns the first node in this command. This will always
     * be a {@link LiteralNode}
     *
     * @return The first node
     */
    @NotNull
    LiteralNode<A> firstNode();

    /**
     * Tests this command with the given input, returning a {@link Potential}
     * which contains the parameters parsed as well as any other errors
     * that occurred during execution.
     *
     * @param actor The actor executing the command
     * @param input The input
     * @return The {@link Potential} result
     */
    @NotNull
    @CheckReturnValue
    Potential<A> test(@NotNull A actor, @NotNull MutableStringStream input);

    /**
     * Returns an immutable {@link List} containing all the nodes
     * in this command.
     *
     * @return All the nodes in the command
     */
    @NotNull
    @Unmodifiable
    List<CommandNode<A>> nodes();

    /**
     * Tests whether this command is secret or not. Secret
     * commands must be explicitly annotated using {@link SecretCommand @SecretCommand}.
     *
     * @return if this command is secret.
     */
    boolean isSecret();

    /**
     * Unregisters this executable command.
     */
    void unregister();

    /**
     * Tests whether this command is visible to the actor or not.
     * <p>
     * This involves checking if the command for:
     * <ol>
     *     <li>Secrecy ({@link SecretCommand}): As these cannot be seen by any command</li>
     *     <li>Permission: If the actor has permission to use the command</li>
     * </ol>
     *
     * @param actor Actor to show the message to
     * @return if th
     */
    default boolean isVisibleTo(@NotNull A actor) {
        return !isSecret() && permission().isExecutableBy(actor);
    }

}

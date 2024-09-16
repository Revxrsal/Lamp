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

import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.*;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.*;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.help.Help;
import revxrsal.commands.help.Help.RelatedCommands;
import revxrsal.commands.node.*;
import revxrsal.commands.process.CommandCondition;
import revxrsal.commands.stream.MutableStringStream;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

import static revxrsal.commands.util.Preconditions.notNull;

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
    @Range(from = 1, to = Integer.MAX_VALUE)
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
     * Returns the explicit priority defined by {@link CommandPriority}.
     *
     * @return The command priority
     */
    @NotNull
    OptionalInt commandPriority();

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
     * Executes this command with the given actor and input.
     *
     * @param actor Actor to execute the command as
     * @param input The input to execute with
     */
    default void execute(@NotNull A actor, @NotNull MutableStringStream input) {
        lamp().registry().execute(actor, this, input);
    }

    /**
     * Executes this command with the given context. It is the caller's
     * responsibility to ensure that all required parameters are
     * supplied in the context.
     * <p>
     * Note that this will check all {@link CommandCondition} registered
     * in the {@link #lamp()} instance.
     *
     * @param context The context to execute with
     */
    void execute(@NotNull ExecutionContext<A> context);

    /**
     * Returns the action of this command.
     *
     * @return The command action
     */
    default @NotNull CommandAction<A> action() {
        return Objects.requireNonNull(lastNode().action(), "lastNode().action() is null");
    }

    /**
     * Returns the annotations on the function
     *
     * @return The function annotations
     */
    default @NotNull AnnotationList annotations() {
        return function().annotations();
    }

    /**
     * Returns all related commands of this command. This includes
     * all sibling commands as well as children commands.
     *
     * @param filterFor Actor to filter entries for, by checking permissions
     * @return The related commands
     */
    @NotNull RelatedCommands<A> relatedCommands(@Nullable A filterFor);

    /**
     * Returns all related commands of this command. This includes
     * all sibling commands as well as children commands.
     *
     * @return The related commands
     */
    @NotNull default RelatedCommands<A> relatedCommands() {
        return relatedCommands(null);
    }

    /**
     * Returns all children commands of this command.
     *
     * @param filterFor Actor to filter entries for, by checking permissions
     * @return All children commands.
     */
    @NotNull Help.ChildrenCommands<A> childrenCommands(@Nullable A filterFor);

    /**
     * Returns all children commands of this command.
     *
     * @return All children commands.
     */
    @NotNull default Help.ChildrenCommands<A> childrenCommands() {
        return childrenCommands(null);
    }

    /**
     * Returns all siblings of this command
     *
     * @param filterFor Actor to filter entries for, by checking permissions
     * @return All siblings
     */
    @NotNull Help.SiblingCommands<A> siblingCommands(@Nullable A filterFor);

    /**
     * Returns all siblings of this command
     *
     * @return All siblings
     */
    default @NotNull Help.SiblingCommands<A> siblingCommands() {
        return siblingCommands(null);
    }

    /**
     * Returns all the parameters in this command, in the same order
     * they are declared in the command.
     *
     * @return All the parameters
     */
    @NotNull @Unmodifiable @Contract(pure = true)
    Map<String, ParameterNode<A, Object>> parameters();

    /**
     * Returns the parameter with the given name, or {@code null} if
     * it does not exist.
     *
     * @param name The parameter name
     * @param <T>  The parameter type, automatically cast.
     * @return The parameter, or {@code null} if not present.
     */
    default <T> @Nullable ParameterNode<A, T> parameterOrNull(@NotNull String name) {
        notNull(name, "parameter name");
        //noinspection unchecked
        return (ParameterNode<A, T>) parameters().get(name);
    }

    /**
     * Returns the parameter with the given name, or throws {@link IllegalArgumentException} if
     * it does not exist.
     *
     * @param name The parameter name
     * @param <T>  The parameter type, automatically cast.
     * @return The parameter
     * @throws IllegalArgumentException if it does not exist
     */
    default <T> @NotNull ParameterNode<A, T> parameter(@NotNull String name) {
        ParameterNode<A, T> parameter = parameterOrNull(name);
        if (parameter == null)
            throw new IllegalArgumentException("No such parameter: " + name);
        return parameter;
    }

    /**
     * Tests whether are these two commands siblings.
     *
     * @param command Command to compare with
     * @return true if they are siblings, false if otherwise.
     */
    boolean isSiblingOf(@NotNull ExecutableCommand<A> command);

    /**
     * Tests whether this command is a child of the provided command.
     *
     * @param command Command to compare with
     * @return true if it is a child, false if otherwise.
     */
    boolean isChildOf(@NotNull ExecutableCommand<A> command);

    /**
     * Tests whether the provided command is a child of this command.
     *
     * @param command Command to compare with
     * @return true if it is a child, false if otherwise.
     */
    default boolean isParentOf(@NotNull ExecutableCommand<A> command) {
        return command.isChildOf(this);
    }

    /**
     * Tests whether is the provided command related to this command
     * or not. This tests if it is a sibling or a child.
     *
     * @param command Command to test
     * @return true if related, false if otherwise.
     */
    default boolean isRelatedTo(@NotNull ExecutableCommand<A> command) {
        return isParentOf(command) || isSiblingOf(command);
    }

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

    /**
     * Tests whether the command has any {@link Flag} or {@link Switch}
     * parameters
     *
     * @return if the command contains {@link Flag} or {@link Switch}
     */
    boolean containsFlags();
}

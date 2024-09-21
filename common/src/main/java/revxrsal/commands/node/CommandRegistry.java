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

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a registry of {@link ExecutableCommand commands} maintained by a
 * {@link Lamp} instance.
 * <p>
 * This can be accessed with {@link Lamp#registry()}
 *
 * @param <A> The actor type
 */
public interface CommandRegistry<A extends CommandActor> extends Iterable<ExecutableCommand<A>> {

    /**
     * Returns the underlying {@link Lamp} instance of this
     * registry.
     *
     * @return The Lamp instance
     */
    @NotNull @Contract(pure = true) Lamp<A> lamp();

    /**
     * Executes the given input on the behalf of the actor.
     *
     * @param actor The actor to execute the command with
     * @param input The input to execute with
     */
    default void execute(@NotNull A actor, @NotNull String input) {
        execute(actor, StringStream.create(input));
    }

    /**
     * Executes the given input on the behalf of the actor.
     *
     * @param actor The actor to execute the command with
     * @param input The input to execute with
     */
    void execute(@NotNull A actor, @NotNull StringStream input);

    /**
     * Executes the given command on the behalf of the actor. This
     * is faster than {@link #execute(CommandActor, StringStream)} as it does
     * not involve searching for the command.
     *
     * @param actor The actor to execute the command with
     * @param input The input to execute with
     */
    void execute(@NotNull A actor, @NotNull ExecutableCommand<A> command, @NotNull MutableStringStream input);

    /**
     * Gets an unmodifiable view of all the {@link ExecutableCommand commands}
     * registered in this registry.
     *
     * @return all the registered commands
     */
    @NotNull @UnmodifiableView
    List<ExecutableCommand<A>> commands();

    /**
     * Unregisters the given command.
     *
     * @param command The command to unregister.
     */
    void unregister(@NotNull ExecutableCommand<A> command);

    /**
     * Unregisters all commands that match a certain {@link Predicate} criteria.
     *
     * @param matches Criteria to test for
     */
    void unregisterIf(@NotNull Predicate<@NotNull ExecutableCommand<A>> matches);

    /**
     * Tests whether any of the registered commands matches the
     * given predicate
     *
     * @param matches Criteria to test for
     * @return if any command matches the predicate
     */
    boolean any(@NotNull Predicate<@NotNull ExecutableCommand<A>> matches);

    /**
     * Returns a new list of all commands that match the
     * given predicate
     *
     * @param filterPredicate Criteria to test for
     * @return a list of all commands that match the predicate
     */
    @NotNull @CheckReturnValue @Contract("_ -> new")
    List<ExecutableCommand<A>> filter(@NotNull Predicate<@NotNull ExecutableCommand<A>> filterPredicate);

    /**
     * Returns an immutable iterator of all entries in this registry.
     * <p>
     * Attempting to modify the iterator will throw a {@link UnsupportedOperationException}.
     *
     * @return The iterator
     */
    @NotNull @UnmodifiableView Iterator<ExecutableCommand<A>> iterator();

}

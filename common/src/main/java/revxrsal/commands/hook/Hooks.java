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
package revxrsal.commands.hook;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

import static revxrsal.commands.util.Preconditions.notNull;

/**
 * An immutable registry of {@link Hook}s, which are listeners that
 * allow to execute code at certain points of the command flow.
 * <p>
 * Each {@link Lamp} instance maintains a {@link Hooks} registry,
 * which may be modified using {@link Lamp.Builder#hooks()}.
 *
 * @param <A> The actor type
 * @see Hook
 */
public final class Hooks<A extends CommandActor> {

    /**
     * The registered hooks
     */
    private final @Unmodifiable List<Hook> hooks;

    private Hooks(Builder<A> builder) {
        this.hooks = List.copyOf(builder.hooks);
    }

    /**
     * Calls all {@link CommandRegisteredHook registration hooks}.
     *
     * @param command The command that was registered
     * @return whether any of the hooks cancelled the registration
     */
    @ApiStatus.Internal
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean onCommandRegistered(@NotNull ExecutableCommand<A> command) {
        CancelHandle cancelHandle = newCancelHandle();
        for (Hook hook : hooks) {
            if (hook instanceof CommandRegisteredHook registeredHook)
                registeredHook.onRegistered(command, cancelHandle);
        }
        return cancelHandle.wasCancelled();
    }

    /**
     * Calls all {@link CommandUnregisteredHook un-registration hooks}.
     *
     * @param command The command that was unregistered
     * @return whether any of the hooks cancelled the un-registration
     */
    @ApiStatus.Internal
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean onCommandUnregistered(@NotNull ExecutableCommand<A> command) {
        CancelHandle cancelHandle = newCancelHandle();
        for (Hook hook : hooks) {
            if (hook instanceof CommandUnregisteredHook unregisteredHook)
                unregisteredHook.onUnregistered(command, cancelHandle);
        }
        return cancelHandle.wasCancelled();
    }

    /**
     * Calls all {@link CommandExecutedHook execution hooks}.
     *
     * @param command The command that was executed
     * @param context The execution context
     * @return whether any of the hooks cancelled the execution
     */
    @ApiStatus.Internal
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean onCommandExecuted(@NotNull ExecutableCommand<A> command, @NotNull ExecutionContext<A> context) {
        CancelHandle cancelHandle = newCancelHandle();
        for (Hook hook : hooks) {
            if (hook instanceof CommandExecutedHook executedHook)
                executedHook.onExecuted(command, context, cancelHandle);
        }
        return cancelHandle.wasCancelled();
    }

    /**
     * Creates a new hooks {@link Builder}
     *
     * @param <A> The actor
     * @return The newly created builder
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull <A extends CommandActor> Hooks.Builder<A> builder() {
        return new Builder<>();
    }

    /**
     * A builder for {@link Hooks}
     *
     * @param <A> The actor type
     */
    public static class Builder<A extends CommandActor> {

        private final List<Hook> hooks = new ArrayList<>();

        /**
         * Adds a hook that runs before a command is executed
         *
         * @param hook Hook to register
         * @return this builder
         */
        public @NotNull Builder<A> onCommandExecuted(@NotNull CommandExecutedHook<? super A> hook) {
            return hook(hook);
        }

        /**
         * Adds a hook that runs after a command is registered
         *
         * @param hook Hook to register
         * @return this builder
         */
        public @NotNull Builder<A> onCommandRegistered(@NotNull CommandRegisteredHook<? super A> hook) {
            return hook(hook);
        }

        /**
         * Adds a hook that runs after a command is unregistered
         *
         * @param hook Hook to register
         * @return this builder
         */
        public @NotNull Builder<A> onCommandUnregistered(@NotNull CommandUnregisteredHook<? super A> hook) {
            return hook(hook);
        }

        private @NotNull Builder<A> hook(@NotNull Hook hook) {
            notNull(hook, "hook");
            hooks.add(hook);
            return this;
        }

        /**
         * Creates a new {@link Hooks} registry from this builder
         *
         * @return The newly created {@link Hooks} instance
         */
        @Contract(value = "-> new", pure = true)
        public @NotNull Hooks<A> build() {
            return new Hooks<>(this);
        }
    }

    private static @NotNull CancelHandle newCancelHandle() {
        return new BasicCancelHandle();
    }

    private static final class BasicCancelHandle implements CancelHandle {

        private boolean cancelled = false;

        @Override
        public boolean wasCancelled() {
            return cancelled;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }
}

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
package revxrsal.commands.bukkit;

import lombok.AllArgsConstructor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.brigadier.BukkitArgumentTypes;
import revxrsal.commands.bukkit.util.BukkitVersion;
import revxrsal.commands.process.MessageSender;
import revxrsal.commands.util.Lazy;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static revxrsal.commands.bukkit.BukkitVisitors.*;
import static revxrsal.commands.util.Preconditions.notNull;

/**
 * A collective object that contains all Bukkit-only properties and allows for
 * easy customizing and chaining using a builder
 *
 * @param <A> The actor type.
 */
@AllArgsConstructor
public final class BukkitLampConfig<A extends BukkitCommandActor> implements LampBuilderVisitor<A> {

    private final ActorFactory<A> actorFactory;
    private final Supplier<ArgumentTypes<A>> argumentTypes;
    private final JavaPlugin plugin;
    private final String fallbackPrefix;
    private final boolean disableBrigadier;
    private final boolean disableAsyncCompletion;

    /**
     * Returns a new {@link Builder} with the given plugin.
     *
     * @param plugin Plugin to create for
     * @param <A>    The actor type
     * @return The {@link Builder}
     */
    public static <A extends BukkitCommandActor> Builder<A> builder(@NotNull JavaPlugin plugin) {
        notNull(plugin, "plugin");
        return new Builder<>(plugin);
    }

    /**
     * Returns a new {@link BukkitLampConfig} with the given plugin,
     * containing the default settings.
     *
     * @param plugin Plugin to create for
     * @return The {@link Builder}
     */
    public static BukkitLampConfig<BukkitCommandActor> createDefault(@NotNull JavaPlugin plugin) {
        notNull(plugin, "plugin");
        return new BukkitLampConfig<>(
                ActorFactory.defaultFactory(plugin, Optional.empty()),
                () -> BukkitArgumentTypes.<BukkitCommandActor>builder().build(),
                plugin,
                plugin.getName(),
                false,
                false
        );
    }

    @Override public void visit(Lamp.@NotNull Builder<A> builder) {
        builder.accept(legacyColorCodes())
                .accept(bukkitSenderResolver())
                .accept(bukkitParameterTypes())
                .accept(bukkitExceptionHandler())
                .accept(bukkitPermissions())
                .accept(registrationHooks(plugin, actorFactory, fallbackPrefix))
                .accept(pluginContextParameters(plugin));
        if (!disableAsyncCompletion)
            builder.accept(asyncTabCompletion(plugin, actorFactory));
        if (BukkitVersion.isBrigadierSupported() && !disableBrigadier)
            builder.accept(brigadier(plugin, argumentTypes.get(), actorFactory));
    }

    /**
     * Represents a builder for {@link BukkitLampConfig}
     *
     * @param <A> The actor type
     */
    public static class Builder<A extends BukkitCommandActor> {

        // avoid loading BukkitArgumentTypes because it may trigger a
        // ClassNotFoundError
        @SuppressWarnings("Convert2MethodRef")
        private final Supplier<ArgumentTypes.Builder<A>> argumentTypes = Lazy.of(() -> BukkitArgumentTypes.builder());
        private final @NotNull JavaPlugin plugin;
        private ActorFactory<A> actorFactory;
        private boolean disableBrigadier;
        private String fallbackPrefix;
        private Optional<BukkitAudiences> audiences;
        private @Nullable MessageSender<A, ComponentLike> messageSender;
        private boolean disableAsyncCompletion;

        Builder(@NotNull JavaPlugin plugin) {
            this.plugin = plugin;
            this.fallbackPrefix = plugin.getName();
        }

        /**
         * Sets the {@link ActorFactory}. This allows to supply custom implementations for
         * the {@link BukkitLampConfig} interface.
         *
         * @param actorFactory The actor factory
         * @return This builder
         * @see ActorFactory
         */
        public @NotNull Builder<A> actorFactory(@NotNull ActorFactory<A> actorFactory) {
            this.actorFactory = actorFactory;
            return this;
        }

        /**
         * Returns the {@link ArgumentTypes.Builder} of this builder
         *
         * @return The builder
         */
        public @NotNull ArgumentTypes.Builder<A> argumentTypes() {
            return argumentTypes.get();
        }

        /**
         * Applies the given {@link Consumer} on the {@link #argumentTypes()} instance.
         * This allows for easy chaining of the builder instances
         *
         * @param consumer Consumer to apply
         * @return This builder
         */
        public @NotNull Builder<A> argumentTypes(@NotNull Consumer<ArgumentTypes.Builder<A>> consumer) {
            consumer.accept(argumentTypes.get());
            return this;
        }

        /**
         * Disables brigadier integration
         *
         * @return This builder
         */
        public @NotNull Builder<A> disableBrigadier() {
            return disableBrigadier(true);
        }

        /**
         * Disables brigadier integration
         *
         * @return This builder
         */
        public @NotNull Builder<A> disableAsyncCompletion() {
            return disableAsyncCompletion(true);
        }

        /**
         * Disables brigadier integration
         *
         * @return This builder
         */
        public @NotNull Builder<A> disableBrigadier(boolean disabled) {
            this.disableBrigadier = disabled;
            return this;
        }

        /**
         * Disables asynchronous tab completion
         *
         * @return This builder
         */
        public @NotNull Builder<A> disableAsyncCompletion(boolean disabled) {
            this.disableAsyncCompletion = disabled;
            return this;
        }

        /**
         * Disables brigadier integration
         *
         * @return This builder
         */
        public @NotNull Builder<A> fallbackPrefix(String fallbackPrefix) {
            this.fallbackPrefix = fallbackPrefix;
            return this;
        }

        /**
         * Sets the audiences for sending components
         *
         * @return This builder
         */
        public @NotNull Builder<A> fallbackPrefix(@NotNull BukkitAudiences audiences) {
            this.audiences = Optional.of(audiences);
            return this;
        }

        /**
         * Registers the default message sender used by {@link BukkitCommandActor#reply(ComponentLike)}
         *
         * @param messageSender The sender to use
         * @return This builder instance
         */
        public @NotNull Builder<A> messageSender(@Nullable MessageSender<? super A, ComponentLike> messageSender) {
            //noinspection unchecked
            this.messageSender = (MessageSender<A, ComponentLike>) messageSender;
            return this;
        }

        /**
         * Returns a new {@link BukkitLampConfig} from this builder
         *
         * @return The newly created config
         */
        @Contract("-> new")
        public @NotNull BukkitLampConfig<A> build() {
            //noinspection unchecked
            this.actorFactory = (ActorFactory<A>) ActorFactory.defaultFactory(
                    plugin,
                    audiences,
                    messageSender
            );
            return new BukkitLampConfig<>(
                    this.actorFactory,
                    Lazy.of(() -> argumentTypes.get().build()),
                    this.plugin,
                    fallbackPrefix,
                    disableBrigadier,
                    disableAsyncCompletion
            );
        }
    }
}

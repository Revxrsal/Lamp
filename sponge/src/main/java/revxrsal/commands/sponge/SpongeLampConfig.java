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
package revxrsal.commands.sponge;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.sponge.actor.ActorFactory;
import revxrsal.commands.sponge.actor.SpongeCommandActor;

import static revxrsal.commands.sponge.SpongeVisitors.*;
import static revxrsal.commands.util.Preconditions.notNull;

/**
 * A collective object that contains all Sponge-only properties and allows for
 * easy customizing and chaining using a builder
 *
 * @param <A> The actor type.
 */
public final class SpongeLampConfig<A extends SpongeCommandActor> implements LampBuilderVisitor<A> {

    private final ActorFactory<A> actorFactory;
    private final Object plugin;

    private SpongeLampConfig(ActorFactory<A> actorFactory, Object plugin) {
        this.actorFactory = actorFactory;
        this.plugin = plugin;
    }

    /**
     * Returns a new {@link Builder} with the given plugin.
     *
     * @param plugin Plugin to create for
     * @param <A>    The actor type
     * @return The {@link Builder}
     */
    public static <A extends SpongeCommandActor> Builder<A> builder(@NotNull Object plugin) {
        notNull(plugin, "plugin");
        return new Builder<>(plugin);
    }

    /**
     * Returns a new {@link SpongeLampConfig} with the given plugin,
     * containing the default settings.
     *
     * @param plugin Plugin to create for
     * @return The {@link Builder}
     */
    public static SpongeLampConfig<SpongeCommandActor> createDefault(@NotNull Object plugin) {
        notNull(plugin, "plugin");
        return new SpongeLampConfig<>(ActorFactory.defaultFactory(), plugin);
    }

    @Override public void visit(Lamp.@NotNull Builder<A> builder) {
        builder.accept(legacyColorCodes())
                .accept(componentResponseHandlers())
                .accept(spongeSenderResolver())
                .accept(spongeParameterTypes())
                .accept(spongeExceptionHandler())
                .accept(spongePermissions())
                .accept(registrationHooks(plugin))
                .accept(pluginContextParameters(plugin));
    }

    public ActorFactory<A> actorFactory() {
        return actorFactory;
    }

    public Object plugin() {
        return plugin;
    }

    /**
     * Represents a builder for {@link SpongeLampConfig}
     *
     * @param <A> The actor type
     */
    public static class Builder<A extends SpongeCommandActor> {

        private final @NotNull Object plugin;
        private ActorFactory<A> actorFactory;

        Builder(@NotNull Object plugin) {
            this.plugin = plugin;
        }

        /**
         * Sets the {@link ActorFactory}. This allows to supply custom implementations for
         * the {@link SpongeCommandActor} interface.
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
         * Returns a new {@link SpongeLampConfig} from this builder
         *
         * @return The newly created config
         */
        @Contract("-> new")
        public @NotNull SpongeLampConfig<A> build() {
            return new SpongeLampConfig<>(this.actorFactory, this.plugin);
        }
    }
}

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
package revxrsal.commands.geyser;

import org.geysermc.geyser.api.extension.Extension;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.geyser.actor.ActorFactory;
import revxrsal.commands.geyser.actor.GeyserCommandActor;

import static revxrsal.commands.geyser.GeyserVisitors.*;
import static revxrsal.commands.util.Preconditions.notNull;

/**
 * A collective immutable object that contains all Geyser-only properties and allows
 * for easy customizing and chaining using a builder
 *
 * @param extension    The extension to register commands for
 * @param actorFactory The actor factory
 * @param <A>          The actor type.
 */
public record GeyserLampConfig<A extends GeyserCommandActor>(
        @NotNull Extension extension,
        @NotNull ActorFactory<A> actorFactory
) implements LampBuilderVisitor<A> {

    public GeyserLampConfig {
        notNull(extension, "extension");
        notNull(actorFactory, "actor factory");
    }

    /**
     * Returns a new {@link Builder} with the given {@link Extension}.
     *
     * @param extension Extension to create for
     * @param <A>       The actor type
     * @return The {@link Builder}
     */
    @Contract("_ -> new")
    public static <A extends GeyserCommandActor> @NotNull Builder<A> builder(@NotNull Extension extension) {
        notNull(extension, "extension");
        return new Builder<>(extension);
    }

    /**
     * Returns a new {@link GeyserLampConfig} with the given {@link Extension},
     * containing the default settings.
     *
     * @param extension Extension to create for
     * @return The config
     */
    @Contract("_ -> new")
    public static @NotNull GeyserLampConfig<GeyserCommandActor> createDefault(@NotNull Extension extension) {
        return new GeyserLampConfig<>(extension, ActorFactory.defaultFactory());
    }

    @Override public void visit(@NotNull Lamp.Builder<A> builder) {
        builder
                .accept(legacyColorCodes())
                .accept(geyserSenderResolver())
                .accept(geyserParameterTypes())
                .accept(geyserExceptionHandler())
                .accept(geyserPermissions())
                .accept(extensionContextParameters(extension))
                .accept(registrationHooks(extension, actorFactory));
    }

    /**
     * Represents a builder for {@link GeyserLampConfig}
     *
     * @param <A> The actor type
     */
    public static final class Builder<A extends GeyserCommandActor> {

        private final @NotNull Extension extension;
        private ActorFactory<A> actorFactory;

        Builder(@NotNull Extension extension) {
            this.extension = extension;
        }

        /**
         * Sets the {@link ActorFactory}. This allows to supply custom implementations for
         * the {@link GeyserCommandActor} interface.
         *
         * @param actorFactory The actor factory
         * @return This builder
         * @see ActorFactory
         */
        public @NotNull Builder<A> actorFactory(@NotNull ActorFactory<A> actorFactory) {
            this.actorFactory = notNull(actorFactory, "actor factory");
            return this;
        }

        /**
         * Returns a new {@link GeyserLampConfig} from this builder
         *
         * @return The newly created config
         */
        @Contract("-> new")
        @SuppressWarnings("unchecked")
        public @NotNull GeyserLampConfig<A> build() {
            if (actorFactory == null)
                actorFactory = (ActorFactory<A>) ActorFactory.defaultFactory();
            return new GeyserLampConfig<>(extension, actorFactory);
        }
    }
}

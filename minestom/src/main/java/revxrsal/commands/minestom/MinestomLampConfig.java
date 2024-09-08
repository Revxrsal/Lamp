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
package revxrsal.commands.minestom;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.minestom.actor.ActorFactory;
import revxrsal.commands.minestom.actor.MinestomCommandActor;
import revxrsal.commands.minestom.argument.ArgumentTypes;
import revxrsal.commands.minestom.argument.MinestomArgumentTypes;

import java.util.function.Consumer;

import static revxrsal.commands.minestom.MinestomVisitors.*;
import static revxrsal.commands.util.Preconditions.notNull;

/**
 * A collective immutable object that contains all Velocity-only properties and allows
 * for easy customizing and chaining using a builder
 *
 * @param <A> The actor type.
 */
public final class MinestomLampConfig<A extends MinestomCommandActor> implements LampBuilderVisitor<A> {

    private final ActorFactory<A> actorFactory;
    private final ArgumentTypes<A> argumentTypes;

    private MinestomLampConfig(ActorFactory<A> actorFactory, ArgumentTypes<A> argumentTypes) {
        this.actorFactory = actorFactory;
        this.argumentTypes = argumentTypes;
    }

    /**
     * Returns a new {@link Builder}.
     *
     * @param <A> The actor type
     * @return The {@link Builder}
     */
    @Contract("-> new")
    public static <A extends MinestomCommandActor> @NotNull Builder<A> builder() {
        return new Builder<>();
    }

    /**
     * Returns a new {@link MinestomLampConfig} containing the default settings.
     *
     * @return The {@link Builder}
     */
    @Contract("-> new")
    public static @NotNull MinestomLampConfig<MinestomCommandActor> createDefault() {
        return new MinestomLampConfig<>(
                ActorFactory.defaultFactory(),
                MinestomArgumentTypes.<MinestomCommandActor>builder().build()
        );
    }

    @Override public void visit(Lamp.@NotNull Builder<A> builder) {
        builder
                .accept(legacyColorCodes())
                .accept(minestomSenderResolver())
                .accept(minestomParameterTypes())
                .accept(minestomExceptionHandler())
                .accept(minestomPermissions())
                .accept(registrationHooks(actorFactory, argumentTypes))
                .accept(minestomContextParameters());
    }

    public @NotNull ActorFactory<A> actorFactory() {
        return actorFactory;
    }

    public @NotNull ArgumentTypes<A> argumentTypes() {
        return argumentTypes;
    }

    /**
     * Represents a builder for {@link MinestomLampConfig}
     *
     * @param <A> The actor type
     */
    public static class Builder<A extends MinestomCommandActor> {

        private final ArgumentTypes.Builder<A> argumentTypes = MinestomArgumentTypes.builder();
        private ActorFactory<A> actorFactory;

        Builder() {
        }

        /**
         * Sets the {@link ActorFactory}. This allows to supply custom implementations for
         * the {@link MinestomCommandActor} interface.
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
         * Returns the {@link ArgumentTypes.Builder} of this builder
         *
         * @return The builder
         */
        public @NotNull ArgumentTypes.Builder<A> argumentTypes() {
            return argumentTypes;
        }

        /**
         * Applies the given {@link Consumer} on the {@link #argumentTypes()} instance.
         * This allows for easy chaining of the builder instances
         *
         * @param consumer Consumer to apply
         * @return This builder
         */
        public @NotNull Builder<A> argumentTypes(@NotNull Consumer<ArgumentTypes.Builder<A>> consumer) {
            consumer.accept(argumentTypes);
            return this;
        }

        /**
         * Returns a new {@link MinestomLampConfig} from this builder
         *
         * @return The newly created config
         */
        @Contract("-> new")
        public @NotNull MinestomLampConfig<A> build() {
            return new MinestomLampConfig<>(actorFactory, argumentTypes.build());
        }
    }
}

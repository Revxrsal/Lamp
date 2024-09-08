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
package revxrsal.commands.minestom.argument;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProviders;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ParameterNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static revxrsal.commands.util.Preconditions.notNull;

/**
 * Represents an immutable registry for {@link ArgumentTypeFactory}
 *
 * @param <A> The actor type
 */
public final class ArgumentTypes<A extends CommandActor> {

    /**
     * A list of default factories. These are registered at the very last
     * so they do not override user-registered factories.
     * <p>
     * These also will not be included in {@link #toBuilder()}.
     */
    private static final List<ArgumentTypeFactory<?>> DEFAULT_FACTORIES = List.of(
            DefaultTypeFactories.LONG,
            DefaultTypeFactories.INTEGER,
            DefaultTypeFactories.SHORT,
            DefaultTypeFactories.BYTE,
            DefaultTypeFactories.DOUBLE,
            DefaultTypeFactories.FLOAT,
            DefaultTypeFactories.BOOLEAN,
            DefaultTypeFactories.CHAR,
            DefaultTypeFactories.STRING
    );

    private final List<ArgumentTypeFactory<? super A>> factories;
    private final int lastIndex;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ArgumentTypes(@NotNull Builder<A> builder) {
        List<ArgumentTypeFactory<? super A>> factories = new ArrayList<>(builder.factories.size() + DEFAULT_FACTORIES.size());
        factories.addAll(builder.factories);
        //noinspection unchecked
        factories.addAll((Collection) DEFAULT_FACTORIES);
        this.factories = factories;
        this.lastIndex = builder.lastIndex;
    }

    /**
     * Creates a new {@link Builder} for the registry
     *
     * @param <A> The actor type
     * @return The newly created builder
     */
    @Contract(value = "-> new", pure = true)
    public static <A extends CommandActor> @NotNull Builder<A> builder() {
        return new Builder<>();
    }

    /**
     * Returns the first {@link Argument} that can create an argument type
     * for the given parameter.
     * <p>
     * Note that this method will never return {@code null}. In cases where no
     * suitable provider is found, it will return {@link ArgumentType#String(String)}.
     *
     * @param parameter The parameter to create for
     * @return The argument type, or {@link ArgumentType#String(String)}.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NotNull Optional<Argument<?>> type(@NotNull ParameterNode<A, ?> parameter) {
        notNull(parameter, "parameter");
        for (ArgumentTypeFactory<? super A> factory : factories) {
            Argument<?> provider = factory.getArgumentType(((ParameterNode) parameter));
            if (provider != null)
                return Optional.of(provider);
        }
        return Optional.empty();
    }


    /**
     * Creates a {@link SuggestionProviders.Builder} that contains the providers registered
     * in this registry.
     *
     * @return The new builder
     */
    @Contract(value = "-> new", pure = true)
    public @NotNull Builder<A> toBuilder() {
        Builder<A> result = new Builder<>();
        for (int i = lastIndex, limit = factories.size() - DEFAULT_FACTORIES.size(); i < limit; i++) {
            result.addTypeFactoryLast(factories.get(i));
        }
        return result;
    }

    public static class Builder<A extends CommandActor> {

        private final List<ArgumentTypeFactory<? super A>> factories = new ArrayList<>();
        private int lastIndex = 0;

        /**
         * Registers an {@link ArgumentType} that matches a specific class. Note
         * that this will not include subclasses of such class.
         * <p>
         * If you would like to include subclasses, register the factory
         * created by {@link ArgumentTypeFactory#forTypeAndSubclasses(Class, Function)}.
         *
         * @param type         The type to register for
         * @param argumentType The provider
         * @return This builder instance
         */
        public @NotNull Builder<A> addType(Class<?> type, @NotNull Function<ParameterNode<A, ?>, Argument<?>> argumentType) {
            notNull(type, "type");
            addTypeFactory(ArgumentTypeFactory.forType(type, argumentType));
            return this;
        }

        /**
         * Registers an {@link ArgumentType} that matches a specific class. Note
         * that this will not include subclasses of such class.
         * <p>
         * If you would like to include subclasses, register the factory
         * created by {@link ArgumentTypeFactory#forTypeAndSubclasses(Class, Function)}.
         * <p>
         * Note that this will be given lower priority over other providers.
         *
         * @param type         The type to register for
         * @param argumentType The provider
         * @return This builder instance
         */
        public @NotNull <T> Builder<A> addTypeLast(Class<T> type, @NotNull Function<ParameterNode<A, ?>, Argument<?>> argumentType) {
            notNull(type, "type");
            return addTypeFactoryLast(ArgumentTypeFactory.forType(type, argumentType));
        }

        /**
         * Registers an {@link ArgumentTypeFactory} to this registry.
         *
         * @param factory The factory to register
         * @return This builder instance
         */
        public @NotNull Builder<A> addTypeFactory(@NotNull ArgumentTypeFactory<? super A> factory) {
            notNull(factory, "factory");
            factories.add(lastIndex++, factory);
            return this;
        }

        /**
         * Registers an {@link ArgumentTypeFactory} to this registry.
         * <p>
         * Note that this will be given lower priority over other providers.
         *
         * @param factory The factory to register
         * @return This builder instance
         */
        public @NotNull Builder<A> addTypeFactoryLast(@NotNull ArgumentTypeFactory<? super A> factory) {
            notNull(factory, "factory");
            factories.add(factory);
            return this;
        }

        /**
         * Returns a new {@link ArgumentTypes} registry
         *
         * @return The newly created registry
         */
        @Contract(value = "-> new", pure = true)
        public @NotNull ArgumentTypes<A> build() {
            return new ArgumentTypes<>(this);
        }

    }
}

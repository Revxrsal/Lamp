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
package revxrsal.commands.autocomplete;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.autocomplete.SuggestionProvider.Factory;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static revxrsal.commands.util.Preconditions.notNull;

/**
 * An immutable registry of {@link SuggestionProvider SuggestionProviders} and
 * {@link Factory SuggestionProvider.Factories}.
 * <p>
 * This type should be constructed using {@link SuggestionProviders#builder()}.
 *
 * @param <A> The actor type
 */
public final class SuggestionProviders<A extends CommandActor> {

    /**
     * A list of default factories. These are registered at the very last
     * so they do not override user-registered factories.
     * <p>
     * These also will not be included in {@link #toBuilder()}.
     */
    private static final List<Factory<?>> DEFAULT_FACTORIES = List.of(
            new SuggestAnnotationProviderFactory(),
            new SuggestWithProviderFactory()
    );

    private final List<Factory<? super A>> factories;
    private final int lastIndex;

    private SuggestionProviders(Builder<A> builder) {
        List<Factory<? super A>> factories = new ArrayList<>(builder.factories.size() + DEFAULT_FACTORIES.size());
        factories.addAll(builder.factories);
        //noinspection unchecked
        factories.addAll((Collection) DEFAULT_FACTORIES);
        this.factories = factories;
        this.lastIndex = builder.lastIndex;
    }

    /**
     * Creates a new {@link Builder} for {@link SuggestionProviders}.
     *
     * @param <A> The actor type
     * @return The newly created {@link Builder}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull <A extends CommandActor> Builder<A> builder() {
        return new Builder<>();
    }

    /**
     * Returns the first {@link SuggestionProvider} that can create suggestions
     * for the given parameter.
     * <p>
     * Note that this method will never return {@code null}. In cases
     * where no suitable provider is found, it will return {@link SuggestionProvider#empty()}.
     *
     * @param parameter The parameter to create for
     * @param lamp      The {@link Lamp} instance to pass to factories
     * @return The suggestion provider, or {@link SuggestionProvider#empty()}.
     */
    public @NotNull SuggestionProvider<A> provider(@NotNull CommandParameter parameter, @NotNull Lamp<A> lamp) {
        notNull(parameter, "parameter");
        notNull(lamp, "Lamp");
        return provider(parameter.fullType(), parameter.annotations(), lamp);
    }

    /**
     * Returns the first {@link SuggestionProvider} that can create suggestions
     * for the given type and annotations. This method can be used
     * to create more complex suggestion providers for composite types (such as {@code T[]})
     * <p>
     * Note that this method will never return {@code null}. In cases where no
     * suitable provider is found, it will return {@link SuggestionProvider#empty()}.
     *
     * @param type        The type to create for
     * @param annotations The annotations to pass to factories
     * @param lamp        The {@link Lamp} instance to pass to factories
     * @return The suggestion provider, or {@link SuggestionProvider#empty()}.
     */
    @SuppressWarnings("unchecked")
    public @NotNull SuggestionProvider<A> provider(@NotNull Type type, @NotNull AnnotationList annotations, @NotNull Lamp<A> lamp) {
        notNull(type, "type");
        notNull(annotations, "annotations");
        notNull(lamp, "Lamp");
        for (Factory<? super A> factory : factories) {
            SuggestionProvider<A> provider = (SuggestionProvider<A>) factory.create(type, annotations, (Lamp) lamp);
            if (provider != null)
                return provider;
        }
        return SuggestionProvider.empty();
    }

    /**
     * Returns the first {@link SuggestionProvider} that comes after the {@code skipPast}
     * factory. This is useful for adding behavior on top of existing providers.
     *
     * @param type        The type to create for
     * @param annotations The annotations to pass to factories
     * @param skipPast    The factory to skip past. In most cases, this will be the caller factory,
     *                    i.e. {@code this}.
     * @param lamp        The {@link Lamp} instance to pass to factories
     * @return The suggestion provider, or {@link SuggestionProvider#empty()}.
     */
    public @NotNull SuggestionProvider<A> findNextProvider(
            @NotNull Type type,
            @NotNull AnnotationList annotations,
            @NotNull Factory<? super A> skipPast,
            @NotNull Lamp<A> lamp
    ) {
        int skipPastIndex = factories.indexOf(skipPast);
        if (skipPastIndex == -1) {
            throw new IllegalArgumentException("Don't know how to skip past unknown provider factory: " + skipPastIndex + " (it isn't registered?)");
        }

        for (int i = skipPastIndex + 1, size = factories.size(); i < size; i++) {
            Factory<? super A> factory = factories.get(i);
            SuggestionProvider<A> parameterType = factory.create(type, annotations, (Lamp) lamp);
            if (parameterType != null)
                return parameterType;
        }
        return SuggestionProvider.empty();
    }

    /**
     * Creates a {@link Builder} that contains the providers registered
     * in this registry.
     *
     * @return The new builder
     */
    @Contract(value = "-> new", pure = true)
    public @NotNull Builder<A> toBuilder() {
        Builder<A> result = new Builder<>();
        for (int i = 0; i < lastIndex; i++) {
            result.addProviderFactory(factories.get(i));
        }
        for (int i = lastIndex, limit = factories.size() - DEFAULT_FACTORIES.size(); i < limit; i++) {
            result.addProviderFactoryLast(factories.get(i));
        }
        return result;
    }

    public static class Builder<A extends CommandActor> {

        private final List<Factory<? super A>> factories = new ArrayList<>();
        private int lastIndex = 0;

        /**
         * Registers a {@link SuggestionProvider} that matches a specific class. Note
         * that this will not include subclasses of such class.
         * <p>
         * If you would like to include subclasses, register the factory
         * created by {@link Factory#forTypeAndSubclasses(Class, SuggestionProvider)}.
         *
         * @param type     The type to register for
         * @param provider The provider
         * @return This builder instance
         */
        public Builder<A> addProvider(@NotNull Class<?> type, @NotNull SuggestionProvider<A> provider) {
            addProviderFactory(Factory.forType(type, provider));
            return this;
        }

        /**
         * Registers a {@link SuggestionProvider} that matches a specific class. Note
         * that this will not include subclasses of such class.
         * <p>
         * If you would like to include subclasses, register the factory
         * created by {@link Factory#forTypeAndSubclasses(Class, SuggestionProvider)}.
         * <p>
         * Note that this will be given lower priority over other providers.
         *
         * @param type     The type to register for
         * @param provider The provider
         * @return This builder instance
         */
        public Builder<A> addProviderLast(@NotNull Class<?> type, @NotNull SuggestionProvider<A> provider) {
            notNull(type, "type");
            addProviderFactoryLast(Factory.forType(type, provider));
            return this;
        }

        /**
         * Registers a {@link SuggestionProvider} that matches all parameters with a specific annotation.
         *
         * @param type     The type to register for
         * @param provider The provider
         * @return This builder instance
         */
        public Builder<A> addProviderForAnnotation(@NotNull Class<? extends Annotation> type, @NotNull SuggestionProvider<A> provider) {
            addProviderFactory(Factory.forAnnotation(type, provider));
            return this;
        }

        /**
         * Registers a {@link SuggestionProvider} that matches all parameters with a specific
         * annotation.
         * <p>
         * Note that this will be given lower priority over other providers.
         *
         * @param type     The type to register for
         * @param provider The provider
         * @return This builder instance
         */
        public Builder<A> addProviderForAnnotationLast(@NotNull Class<? extends Annotation> type, @NotNull SuggestionProvider<A> provider) {
            addProviderFactoryLast(Factory.forAnnotation(type, provider));
            return this;
        }

        /**
         * Registers a {@link Factory} to this registry.
         *
         * @param factory The factory to register
         * @return This builder instance
         */
        public Builder<A> addProviderFactory(@NotNull SuggestionProvider.Factory<? super A> factory) {
            notNull(factory, "factory");
            factories.add(lastIndex++, factory);
            return this;
        }

        /**
         * Registers a {@link Factory} to this registry.
         * <p>
         * Note that this will be given lower priority over other providers.
         *
         * @param factory The factory to register
         * @return This builder instance
         */
        public Builder<A> addProviderFactoryLast(@NotNull SuggestionProvider.Factory<? super A> factory) {
            notNull(factory, "factory");
            factories.add(factory);
            return this;
        }

        /**
         * Creates a new {@link SuggestionProviders} instance of this builder
         *
         * @return The newly created {@link SuggestionProviders} registry.
         */
        @Contract(pure = true, value = "-> new")
        public @NotNull SuggestionProviders<A> build() {
            return new SuggestionProviders<>(this);
        }
    }
}

/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
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
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static revxrsal.commands.util.Classes.checkRetention;

/**
 * An interface that supplies completions for the user depending on their input.
 */
@FunctionalInterface
public interface SuggestionProvider<A extends CommandActor> extends BaseSuggestionProvider {

    /**
     * Returns a {@link SuggestionProvider} that always gives empty suggestions.
     *
     * @param <A> The actor type
     * @return The empty suggestion provider singleton
     */
    static <A extends CommandActor> @NotNull SuggestionProvider<A> empty() {
        //noinspection unchecked
        return (SuggestionProvider<A>) EmptySuggestionProvider.INSTANCE;
    }

    /**
     * Creates a {@link SuggestionProvider} that provides suggestions asynchronously.
     * <p>
     * Note: The ability to provide asynchronous completions is platform-dependent.
     * In platforms where such behavior is unsupported, Lamp will fall back to
     * normal completions, and {@link AsyncSuggestionProvider#getSuggestionsAsync(ExecutionContext)} will
     * block.
     *
     * @param provider The asynchronous provider
     * @param <A>      The actor type
     * @return The {@link SuggestionProvider}
     */
    @Contract("_ -> new")
    static <A extends CommandActor> @NotNull SuggestionProvider<A> fromAsync(@NotNull AsyncSuggestionProvider<A> provider) {
        return new WrapperAsyncSuggestionProvider<>(provider);
    }

    /**
     * Returns a {@link SuggestionProvider} that provides a static list of suggestions
     *
     * @param suggestions Suggestions to provide
     * @param <A>         The actor type
     * @return The suggestion provider.
     */
    static <A extends CommandActor> @NotNull SuggestionProvider<A> of(@NotNull String... suggestions) {
        if (suggestions == null || suggestions.length == 0)
            return empty();
        List<String> list = Arrays.asList(suggestions);
        return (context) -> list;
    }

    /**
     * Returns a {@link SuggestionProvider} that provides a static list of suggestions
     *
     * @param suggestions Suggestions to provide
     * @param <A>         The actor type
     * @return The suggestion provider.
     */
    static <A extends CommandActor> @NotNull SuggestionProvider<A> of(@NotNull List<String> suggestions) {
        if (suggestions.isEmpty())
            return empty();
        return (context) -> suggestions;
    }

    /**
     * Returns the suggestions
     *
     * @param context The execution context. This will try to parse
     *                arguments inputted by the user and store them
     *                to provide context-aware suggestions.
     * @return The command suggestions.
     */
    @NotNull
    Collection<String> getSuggestions(@NotNull ExecutionContext<A> context);

    /**
     * Represents a factory that creates {@link SuggestionProvider}s dynamically. This
     * can access the parameter type, generics and annotations.
     *
     * @param <A> The actor type
     */
    interface Factory<A extends CommandActor> extends BaseSuggestionProvider {

        /**
         * Returns a {@link Factory} that returns a suggestion provider for
         * all parameters that match a certain type. Note that this does <em>not</em>
         * include the subclasses of such type.
         *
         * @param type     Type to provide suggestions for
         * @param provider The suggestion provider
         * @param <A>      The actor type
         * @return The newly created {@link Factory}.
         */
        static <A extends CommandActor> Factory<? super A> forType(@NotNull Class<?> type, @NotNull SuggestionProvider<A> provider) {
            return new ClassSuggestionProviderFactory<>(type, provider, false);
        }

        /**
         * Returns a {@link Factory} that returns a suggestion provider for
         * all parameters that match a certain type, as well as its subclasses.
         *
         * @param type     Type to provide suggestions for
         * @param provider The suggestion provider
         * @param <A>      The actor type
         * @return The newly created {@link Factory}.
         */
        static <A extends CommandActor> Factory<? super A> forTypeAndSubclasses(@NotNull Class<?> type, @NotNull SuggestionProvider<A> provider) {
            return new ClassSuggestionProviderFactory<>(type, provider, true);
        }

        /**
         * Returns a {@link Factory} that returns a suggestion provider for
         * all parameters that contain a specific annotation
         *
         * @param annotationType The annotation type to provide suggestions for
         * @param provider       The suggestion provider
         * @param <A>            The actor type
         * @return The newly created {@link Factory}.
         */
        static @NotNull <A extends CommandActor, L extends Annotation> SuggestionProvider.@NotNull Factory<? super A> forAnnotation(@NotNull Class<L> annotationType, @NotNull Function<L, SuggestionProvider<A>> provider) {
            checkRetention(annotationType);
            return (type, annotations, lamp) -> {
                L annotation = annotations.get(annotationType);
                if (annotation != null)
                    return provider.apply(annotation);
                return null;
            };
        }

        /**
         * Creates a {@link SuggestionProvider} for the given parameter. If
         * this parameter is not applicable, {@code null} should be returned.
         *
         * @param type        Type to create for
         * @param annotations The type annotations
         * @param lamp        The Lamp instance
         * @return The suggestion provider for the parameter, or {@code null}
         * if not applicable.
         */
        @Nullable SuggestionProvider<A> create(@NotNull Type type, @NotNull AnnotationList annotations, @NotNull Lamp<A> lamp);

    }
}

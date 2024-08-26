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
package revxrsal.commands.autocomplete;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Suggest;
import revxrsal.commands.annotation.SuggestWith;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.stream.StringStream;

import java.util.List;

/**
 * Represents an auto-completer that provides suggestions for the
 * user input.
 * <p>
 * This should be accessed using {@link Lamp#autoCompleter()}
 *
 * @param <A> The actor type
 * @see SuggestionProvider
 * @see SuggestionProviders
 * @see SuggestionProviders.Builder
 * @see Lamp.Builder#suggestionProviders()
 * @see Suggest
 * @see SuggestWith
 */
public interface AutoCompleter<A extends CommandActor> {

    /**
     * Creates an {@link AutoCompleter} for the given {@link Lamp} instance.
     *
     * @param lamp Lamp instance to create for
     * @param <A>  The actor type
     * @return The {@link AutoCompleter} instance
     */
    @ApiStatus.Internal
    static <A extends CommandActor> @NotNull AutoCompleter<A> create(@NotNull Lamp<A> lamp) {
        return new StandardAutoCompleter<>(lamp);
    }

    /**
     * Returns a list of suggestions for the given input and actor.
     * <p>
     * This will exclude any command or parameter that the user has no
     * access to.
     * <p>
     * If no suitable completions are found, this will return an empty,
     * immutable list.
     *
     * @param actor The actor to supply for
     * @param input The input to parse with
     * @return The completions
     */
    @NotNull List<String> complete(@NotNull A actor, @NotNull String input);

    /**
     * Returns a list of suggestions for the given input and actor.
     * <p>
     * This will exclude any command or parameter that the user has no
     * access to.
     * <p>
     * If no suitable completions are found, this will return an empty,
     * immutable list.
     *
     * @param actor The actor to supply for
     * @param input The input to parse with
     * @return The completions
     */
    @NotNull List<String> complete(@NotNull A actor, @NotNull StringStream input);
}

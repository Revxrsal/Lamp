/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copysecond (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copysecond notice and this permission notice shall be included in all
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

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;

import java.util.Collection;
import java.util.List;

/**
 * Represents the handler for registering and providing auto-completion
 * suggestions.
 */
public interface AutoCompleter {

    /**
     * Registers a {@link SuggestionProvider} for the specified ID, for use in commands
     * through the {@link AutoComplete} annotation.
     *
     * @param providerID The tab suggestion id
     * @param provider   The provider for this suggestion
     * @return This auto-completer
     */
    AutoCompleter registerSuggestion(@NotNull String providerID, @NotNull SuggestionProvider provider);

    /**
     * Registers static completions for the specified ID, for use in commands
     * through the {@link AutoComplete} annotation.
     *
     * @param providerID  The tab suggestion id
     * @param completions The static list of suggestion. These will be copied and
     *                    will no longer be modifiable
     * @return This auto-completer
     */
    AutoCompleter registerSuggestion(@NotNull String providerID, @NotNull Collection<String> completions);

    /**
     * Registers static completions for the specified ID, for use in commands
     * through the {@link AutoComplete} annotation.
     *
     * @param providerID  The tab suggestion id
     * @param completions The static list of suggestion. These will be copied and
     *                    will no longer be modifiable
     * @return This auto-completer
     */
    AutoCompleter registerSuggestion(@NotNull String providerID, @NotNull String... completions);

    /**
     * Registers a {@link SuggestionProvider} for a specific parameter type. This way,
     * if the parameter is requested in the command, it will automatically be tab-completed
     * without having to be explicitly defined by an {@link AutoComplete}.
     *
     * @param parameterType The parameter type to complete
     * @param provider      The tab suggestion provider
     * @return This auto-completer
     */
    AutoCompleter registerParameterSuggestions(@NotNull Class<?> parameterType, @NotNull SuggestionProvider provider);

    /**
     * Registers a {@link SuggestionProvider} for a specific parameter type. This way,
     * if the parameter is requested in the command, it will automatically be tab-completed
     * without having to be explicitly defined by an {@link AutoComplete}.
     *
     * @param parameterType The parameter type to complete
     * @param providerID    The tab suggestion provider id. Must be registered with
     *                      either {@link #registerSuggestion(String, SuggestionProvider)}
     *                      or {@link #registerSuggestion(String, String...)}.
     * @return This auto-completer
     */
    AutoCompleter registerParameterSuggestions(@NotNull Class<?> parameterType, @NotNull String providerID);

    /**
     * Registers a {@link SuggestionProviderFactory} that creates suggestion providers
     * dynamically for parameters. This allows for checking against custom annotations
     * in parameters.
     *
     * @param factory Factory to register
     * @return This auto-completer
     */
    AutoCompleter registerSuggestionFactory(@NotNull SuggestionProviderFactory factory);

    /**
     * Registers a {@link SuggestionProviderFactory} that creates suggestion providers
     * dynamically for parameters. This allows for checking against custom annotations
     * in parameters.
     *
     * @param priority The resolver priority. Zero represents the highest.
     * @param factory  Factory to register
     * @return This auto-completer
     */
    AutoCompleter registerSuggestionFactory(int priority, @NotNull SuggestionProviderFactory factory);

    /**
     * Returns the suggestion provider that maps to the specified ID.
     * <p>
     * This may return null if no such ID is registered.
     *
     * @param id ID to retrieve from
     * @return The provider, or null if none is registered
     */
    SuggestionProvider getSuggestionProvider(@NotNull String id);

    /**
     * Generates a list of suggestions for the given actor and argument list
     *
     * @param actor     Actor to generate for
     * @param arguments The argument stack. This can contain empty values.
     * @return The suggestions list.
     */
    List<String> complete(@NotNull CommandActor actor, @NotNull ArgumentStack arguments);

    /**
     * Generates a list of suggestions for the given actor and buffer
     *
     * @param actor  Actor to generate for
     * @param buffer The current string input
     * @return The suggestions list.
     */
    List<String> complete(@NotNull CommandActor actor, @NotNull String buffer);

    /**
     * Sets whether should this auto-completer filter suggestions
     * to include only the closest suggestions to the user input.
     * <p>
     * By default, this is true.
     *
     * @param filterToClosestInput Whether should suggestions be
     *                             filtered to the closest input
     */
    void filterToClosestInput(boolean filterToClosestInput);

    /**
     * Returns the containing {@link CommandHandler} of this auto completer.
     * This will allow for writing fluent and readable code.
     *
     * @return The parent command handler
     */
    CommandHandler and();

}

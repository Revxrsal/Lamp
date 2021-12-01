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
     * @param factory Factory to register
     * @return This auto-completer
     */
    AutoCompleter registerSuggestionFactory(@NotNull SuggestionProviderFactory factory);

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
     * Returns the containing {@link CommandHandler} of this auto completer.
     * This will allow for writing fluent and readable code.
     *
     * @return The parent command handler
     */
    CommandHandler and();

}

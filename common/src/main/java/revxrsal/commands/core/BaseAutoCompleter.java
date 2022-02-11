package revxrsal.commands.core;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.autocomplete.AutoCompleter;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.autocomplete.SuggestionProviderFactory;
import revxrsal.commands.command.*;
import revxrsal.commands.util.Primitives;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static revxrsal.commands.util.Collections.listOf;
import static revxrsal.commands.util.Preconditions.notNull;

final class BaseAutoCompleter implements AutoCompleter {

    private final BaseCommandHandler handler;
    final Map<String, SuggestionProvider> suggestionKeys = new HashMap<>();
    final LinkedList<SuggestionProviderFactory> factories = new LinkedList<>();

    public BaseAutoCompleter(BaseCommandHandler handler) {
        this.handler = handler;
        registerSuggestionFactory(EnumSuggestionProviderFactory.INSTANCE);
        registerSuggestion("nothing", Collections.emptyList());
        registerSuggestion("empty", Collections.emptyList());
        registerParameterSuggestions(boolean.class, SuggestionProvider.of("true", "false"));
        registerSuggestionFactory(new AutoCompleterAnnotationFactory(suggestionKeys));
    }

    @Override public AutoCompleter registerSuggestion(@NotNull String providerID, @NotNull SuggestionProvider provider) {
        notNull(provider, "provider ID");
        notNull(provider, "tab suggestion provider");
        suggestionKeys.put(providerID, provider);
        return this;
    }

    @Override public AutoCompleter registerSuggestion(@NotNull String providerID, @NotNull Collection<String> completions) {
        notNull(providerID, "provider ID");
        notNull(completions, "completions");
        suggestionKeys.put(providerID, (args, sender, command) -> completions);
        return this;
    }

    @Override public AutoCompleter registerSuggestion(@NotNull String providerID, @NotNull String... completions) {
        registerSuggestion(providerID, listOf(completions));
        return this;
    }

    @Override public AutoCompleter registerParameterSuggestions(@NotNull Class<?> parameterType, @NotNull SuggestionProvider provider) {
        notNull(parameterType, "parameter type");
        notNull(provider, "provider");
        registerSuggestionFactory(SuggestionProviderFactory.forType(parameterType, provider));
        Class<?> wrapped = Primitives.wrap(parameterType);
        if (wrapped != parameterType) {
            registerSuggestionFactory(SuggestionProviderFactory.forType(wrapped, provider));
        }
        return this;
    }

    @Override public AutoCompleter registerParameterSuggestions(@NotNull Class<?> parameterType, @NotNull String providerID) {
        notNull(parameterType, "parameter type");
        notNull(providerID, "provider ID");
        SuggestionProvider provider = suggestionKeys.get(providerID);
        if (provider == null) {
            throw new IllegalArgumentException("No such tab provider: " + providerID + ". Available: " + suggestionKeys.keySet());
        }
        registerParameterSuggestions(parameterType, provider);
        return this;
    }

    @Override public AutoCompleter registerSuggestionFactory(@NotNull SuggestionProviderFactory factory) {
        notNull(factory, "suggestion provider factory cannot be null!");
        factories.add(factory);
        return this;
    }

    @Override public AutoCompleter registerSuggestionFactory(int priority, @NotNull SuggestionProviderFactory factory) {
        notNull(factory, "suggestion provider factory cannot be null!");
        factories.add(Math.max(priority, factories.size()), factory);
        return this;
    }

    public SuggestionProvider getProvider(CommandParameter parameter) {
        for (SuggestionProviderFactory factory : factories) {
            SuggestionProvider provider = factory.createSuggestionProvider(parameter);
            if (provider == null) continue;
            return provider;
        }
        return SuggestionProvider.EMPTY;
    }

    @Override public SuggestionProvider getSuggestionProvider(@NotNull String id) {
        return suggestionKeys.get(id);
    }

    @Override public List<String> complete(@NotNull CommandActor actor, @NotNull ArgumentStack arguments) {
        CommandPath path = CommandPath.get(arguments.subList(0, arguments.size() - 1));
        int originalSize = arguments.size();
        ExecutableCommand command = searchForCommand(path, actor);
        if (command != null) {
            command.getPath().forEach(c -> arguments.removeFirst());
            return getCompletions(actor, arguments, command);
        }
        CommandCategory category = getLastCategory(path);
        if (category == null)
            return emptyList();

        category.getPath().forEach(c -> arguments.removeFirst());
        return getCompletions(actor, arguments, category, originalSize);
    }

    @Override public List<String> complete(@NotNull CommandActor actor, @NotNull String buffer) {
        return complete(actor, ArgumentStack.fromString(buffer));
    }

    private ExecutableCommand searchForCommand(CommandPath path, CommandActor actor) {
        ExecutableCommand found = handler.getCommand(path);
        if (found != null && !found.isSecret() && found.getPermission().canExecute(actor)) return found;
        MutableCommandPath mpath = MutableCommandPath.empty();
        for (String p : path) {
            mpath.add(p);
            found = handler.getCommand(mpath);
            if (found != null && !found.isSecret() && found.getPermission().canExecute(actor))
                return found;
        }
        return null;
    }

    private CommandCategory getLastCategory(CommandPath path) {
        MutableCommandPath mpath = MutableCommandPath.empty();
        CommandCategory category = null;
        for (String p : path) {
            mpath.add(p);
            CommandCategory c = handler.getCategory(mpath);
            if (c == null && category != null)
                return category;
            if (c != null)
                category = c;
        }
        return category;
    }

    @SneakyThrows private List<String> getCompletions(CommandActor actor,
                                                      ArgumentStack args,
                                                      @NotNull ExecutableCommand command) {
        try {
            if (args.isEmpty()) return emptyList();
            if (command.getValueParameters().isEmpty()) return emptyList();
            for (CommandParameter parameter : command.getParameters()) {
                try {
                    if (parameter.isSwitch()) {
                        return listOf(handler.switchPrefix + parameter.getSwitchName());
                    }
                    if (parameter.isFlag()) {
                        int index = args.indexOf(handler.getFlagPrefix() + parameter.getFlagName());
                        if (index == -1) {
                            return listOf(handler.getFlagPrefix() + parameter.getFlagName() + " ");
                        } else if (index == args.size() - 2) {
                            SuggestionProvider provider = parameter.getSuggestionProvider();
                            return provider.getSuggestions(args, actor, command)
                                    .stream()
                                    .filter(c -> c.toLowerCase().startsWith(args.getLast().toLowerCase()))
                                    .sorted(String.CASE_INSENSITIVE_ORDER)
                                    .distinct()
                                    .collect(Collectors.toList());
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
            CommandParameter parameter = command.getValueParameters().get(args.size() - 1);
            if (parameter == null) return emptyList(); // extra arguments
            SuggestionProvider provider = parameter.getSuggestionProvider();
            notNull(provider, "provider must not be null!");
            return provider.getSuggestions(args, actor, command)
                    .stream()
                    .filter(c -> c.toLowerCase().startsWith(args.getLast().toLowerCase()))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IndexOutOfBoundsException e) {
            return emptyList();
        }
    }

    private List<String> getCompletions(CommandActor actor, @Unmodifiable ArgumentStack args, CommandCategory category, int originalSize) {
        if (args.isEmpty()) return emptyList();
        Set<String> suggestions = new HashSet<>();
        if (category.getDefaultAction() != null) {
            suggestions.addAll(getCompletions(actor, args, category.getDefaultAction()));
        }
        if (originalSize - category.getPath().size() == 1) {
            category.getCommands().values().forEach(c -> {
                if (!c.isSecret() && c.getPermission().canExecute(actor)) suggestions.add(c.getName());
            });
            category.getCategories().values().forEach(c -> {
                if (c.getPermission().canExecute(actor)) suggestions.add(c.getName());
            });
        }
        return suggestions
                .stream()
                .filter(c -> c.toLowerCase().startsWith(args.getLast().toLowerCase()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override public CommandHandler and() {
        return handler;
    }
}

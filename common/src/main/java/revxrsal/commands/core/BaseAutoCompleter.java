package revxrsal.commands.core;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.autocomplete.AutoCompleter;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.*;
import revxrsal.commands.util.ClassMap;
import revxrsal.commands.util.Strings;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static revxrsal.commands.util.Collections.listOf;
import static revxrsal.commands.util.Preconditions.notNull;
import static revxrsal.commands.util.Strings.VERTICAL_BAR;

final class BaseAutoCompleter implements AutoCompleter {

    private final BaseCommandHandler handler;
    final Map<String, SuggestionProvider> tabProviders = new HashMap<>();
    final ClassMap<SuggestionProvider> parameterTabs = new ClassMap<>();

    public BaseAutoCompleter(BaseCommandHandler handler) {
        this.handler = handler;
        registerSuggestion("nothing", Collections.emptyList());
        registerSuggestion("empty", Collections.emptyList());
        registerParameterSuggestions(boolean.class, SuggestionProvider.of("true", "false"));
    }

    @Override public AutoCompleter registerSuggestion(@NotNull String providerID, @NotNull SuggestionProvider provider) {
        notNull(provider, "provider ID");
        notNull(provider, "tab suggestion provider");
        tabProviders.put(providerID, provider);
        return this;
    }

    @Override public AutoCompleter registerSuggestion(@NotNull String providerID, @NotNull Collection<String> completions) {
        notNull(providerID, "provider ID");
        notNull(completions, "completions");
        tabProviders.put(providerID, (args, sender, command) -> completions);
        return this;
    }

    @Override public AutoCompleter registerSuggestion(@NotNull String providerID, @NotNull String... completions) {
        registerSuggestion(providerID, listOf(completions));
        return this;
    }

    @Override public AutoCompleter registerParameterSuggestions(@NotNull Class<?> parameterType, @NotNull SuggestionProvider provider) {
        notNull(parameterType, "parameter type");
        notNull(provider, "provider");
        parameterTabs.add(parameterType, provider);
        return this;
    }

    @Override public AutoCompleter registerParameterSuggestions(@NotNull Class<?> parameterType, @NotNull String providerID) {
        notNull(parameterType, "parameter type");
        notNull(providerID, "provider ID");
        SuggestionProvider provider = tabProviders.get(providerID);
        if (provider == null) {
            throw new IllegalArgumentException("No such tab provider: " + providerID + ". Available: " + tabProviders.keySet());
        }
        registerParameterSuggestions(parameterType, provider);
        return this;
    }

    @Override public SuggestionProvider getSuggestionProvider(@NotNull String id) {
        return tabProviders.get(id);
    }

    @Override public List<String> complete(@NotNull CommandActor actor, @NotNull ArgumentStack arguments) {
        CommandPath path = CommandPath.get(arguments.subList(0, arguments.size() - 1));
        int originalSize = arguments.size();
        ExecutableCommand command = searchForCommand(path);
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

    private ExecutableCommand searchForCommand(CommandPath path) {
        ExecutableCommand found = handler.getCommand(path);
        if (found != null) return found;
        MutableCommandPath mpath = MutableCommandPath.empty();
        for (String p : path) {
            mpath.add(p);
            found = handler.getCommand(mpath);
            if (found != null)
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
            category.getCommands().values().forEach(c -> suggestions.add(c.getName()));
            category.getCategories().values().forEach(c -> suggestions.add(c.getName()));
        }
        return suggestions
                .stream()
                .filter(c -> c.toLowerCase().startsWith(args.getLast().toLowerCase()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .distinct()
                .collect(Collectors.toList());
    }

    public SuggestionProvider resolveSuggestionProvider(CommandParameter parameter) {
        AutoComplete ann = parameter.getDeclaringCommand().getAnnotation(AutoComplete.class);
        if (ann != null) {
            return parseTabAnnotation(ann, parameter.getCommandIndex());
        }
        return getParameterTab(parameter.getType());
    }

    @NotNull private SuggestionProvider getParameterTab(Class<?> type) {
        SuggestionProvider pr = parameterTabs.getFlexible(type);
        if (pr != null) return pr;
        if (!type.isEnum()) return SuggestionProvider.EMPTY;
        //noinspection rawtypes
        Class<? extends Enum> enumType = type.asSubclass(Enum.class);
        List<String> values = Arrays.stream(enumType.getEnumConstants()).map(t -> t.name().toLowerCase()).collect(Collectors.toList());
        pr = (args, sender, command) -> values;
        parameterTabs.add(type, pr);
        return pr;
    }

    private SuggestionProvider parseTabAnnotation(@NotNull AutoComplete annotation, int commandIndex) {
        if (annotation.value().isEmpty()) return SuggestionProvider.EMPTY;
        String[] values = Strings.SPACE.split(annotation.value());
        try {
            String providerV = values[commandIndex];
            if (providerV.startsWith("@")) {
                SuggestionProvider provider = tabProviders.get(providerV.substring(1));
                if (provider == null)
                    throw new IllegalStateException("No such tab suggestion provider: " + providerV.substring(1));
                return provider;
            } else {
                List<String> suggestions = Arrays.asList(VERTICAL_BAR.split(providerV));
                return SuggestionProvider.of(suggestions);
            }
        } catch (IndexOutOfBoundsException e) {
            return SuggestionProvider.EMPTY;
        }
    }

    @Override public CommandHandler and() {
        return handler;
    }
}

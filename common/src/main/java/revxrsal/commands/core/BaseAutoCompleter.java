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
import static revxrsal.commands.util.Preconditions.coerceIn;
import static revxrsal.commands.util.Preconditions.notNull;

final class BaseAutoCompleter implements AutoCompleter {

    private final BaseCommandHandler handler;
    final Map<String, SuggestionProvider> suggestionKeys = new HashMap<>();
    final List<SuggestionProviderFactory> factories = new ArrayList<>();
    private boolean filterToClosestInput = true;

    public BaseAutoCompleter(BaseCommandHandler handler) {
        this.handler = handler;
        registerSuggestion("nothing", SuggestionProvider.EMPTY);
        registerSuggestion("empty", SuggestionProvider.EMPTY);
        registerParameterSuggestions(boolean.class, SuggestionProvider.of("true", "false"));
        registerSuggestionFactory(new AutoCompleterAnnotationFactory(suggestionKeys));
        registerSuggestionFactory(EitherSuggestionProviderFactory.INSTANCE);
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
        factories.add(coerceIn(priority, 0, factories.size()), factory);
        return this;
    }

    public SuggestionProvider getProvider(CommandParameter parameter) {
        if (parameter.isSwitch()) {
            return SuggestionProvider.of(handler.switchPrefix + parameter.getSwitchName());
        }
        for (SuggestionProviderFactory factory : factories) {
            SuggestionProvider provider = factory.createSuggestionProvider(parameter);
            if (provider == null) continue;
            return provider;
        }
        if (parameter.getType().isEnum()) {
            return EnumSuggestionProviderFactory.INSTANCE.createSuggestionProvider(parameter);
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
        return complete(actor, ArgumentStack.parseForAutoCompletion(buffer));
    }

    @Override
    public void filterToClosestInput(boolean filterToClosestInput) {
        this.filterToClosestInput = filterToClosestInput;
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
            List<CommandParameter> parameters = new ArrayList<>(command.getValueParameters().values());
            Collections.sort(parameters);
            for (CommandParameter parameter : parameters) {
                try {
                    if (parameter.isFlag()) continue;
                    if (parameter.getCommandIndex() == args.size() - 1) {
                        if (!parameter.getPermission().canExecute(actor)) return emptyList();
                        SuggestionProvider provider = parameter.getSuggestionProvider();
                        notNull(provider, "provider must not be null!");
                        return getParamCompletions(provider.getSuggestions(args, actor, command), args);
                    }
                } catch (Throwable ignored) {
                }
            }
            parameters.removeIf(c -> !c.isFlag());
            if (parameters.isEmpty())
                return emptyList();
            Optional<CommandParameter> currentFlag = parameters.stream().filter(c -> {
                int index = args.indexOf(handler.getFlagPrefix() + c.getFlagName());
                return index == args.size() - 2;
            }).findFirst();
            if (currentFlag.isPresent()) {
                SuggestionProvider provider = currentFlag.get().getSuggestionProvider();
                return getParamCompletions(provider.getSuggestions(args, actor, command), args);
            }
            for (CommandParameter flag : parameters) {
                int index = args.indexOf(handler.getFlagPrefix() + flag.getFlagName());
                if (index == -1) {
                    return listOf(handler.getFlagPrefix() + flag.getFlagName());
                } else if (index == args.size() - 2) {
                    return getParamCompletions(flag.getSuggestionProvider().getSuggestions(args, actor, command), args);
                }
            }
            return emptyList();
        } catch (IndexOutOfBoundsException e) {
            return emptyList();
        }
    }

    @NotNull private List<String> getParamCompletions(Collection<String> provider, ArgumentStack args) {
        return provider
                .stream()
                .filter(c -> !filterToClosestInput || c.toLowerCase().startsWith(args.getLast().toLowerCase()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> getCompletions(CommandActor actor, @Unmodifiable ArgumentStack args, CommandCategory category, int originalSize) {
        if (args.isEmpty()) return emptyList();
        Set<String> suggestions = new HashSet<>();
        if (category.getDefaultAction() != null) {
            ExecutableCommand defaultAction = category.getDefaultAction();
            if (!defaultAction.isSecret() && defaultAction.getPermission().canExecute(actor))
                suggestions.addAll(getCompletions(actor, args, defaultAction));
        }
        if (originalSize - category.getPath().size() == 1) {
            category.getCommands().values().forEach(c -> {
                if (!c.isSecret() && c.getPermission().canExecute(actor)) suggestions.add(c.getName());
            });
            category.getCategories().values().forEach(c -> {
                if (!c.isSecret() && c.getPermission().canExecute(actor)) suggestions.add(c.getName());
            });
        }
        return getParamCompletions(suggestions, args);
    }

    @Override public CommandHandler and() {
        return handler;
    }
}

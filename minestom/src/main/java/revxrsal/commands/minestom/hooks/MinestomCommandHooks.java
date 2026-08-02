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
package revxrsal.commands.minestom.hooks;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.ThrowableFromCommand;
import revxrsal.commands.exception.context.ErrorContext;
import revxrsal.commands.hook.CancelHandle;
import revxrsal.commands.hook.CommandRegisteredHook;
import revxrsal.commands.minestom.actor.ActorFactory;
import revxrsal.commands.minestom.actor.MinestomCommandActor;
import revxrsal.commands.minestom.argument.ArgumentTypes;
import revxrsal.commands.minestom.util.ArgumentRenamer;
import revxrsal.commands.node.*;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;
import revxrsal.commands.util.Permutations;

import java.util.*;

import static revxrsal.commands.minestom.util.MinestomUtils.readIntoLampContext;
import static revxrsal.commands.util.Collections.filter;

public final class MinestomCommandHooks<A extends MinestomCommandActor> implements CommandRegisteredHook<A> {

    private final Map<String, Command> registeredRootNames = new HashMap<>();

    private final ActorFactory<A> actorFactory;
    private final ArgumentTypes<A> argumentTypes;

    public MinestomCommandHooks(
            @NotNull ActorFactory<A> actorFactory,
            @NotNull ArgumentTypes<A> argumentTypes
    ) {
        this.actorFactory = actorFactory;
        this.argumentTypes = argumentTypes;
    }

    @Override public void onRegistered(@NotNull ExecutableCommand<A> command, @NotNull CancelHandle cancelHandle) {
        String name = command.firstNode().name();
        Command minestomCommand = registeredRootNames.computeIfAbsent(name, k -> {
            Command c = new Command(k);
            c.setCondition((sender, cmd) -> {
                A actor = actorFactory.create(sender, command.lamp());
                if (cmd == null) {
                    // Minestom is checking whether to declare this command to the client
                    return command.permission().isExecutableBy(actor);
                } else {
                    // For actual execution, command conditions should be left to run for user feedback
                    return true;
                }
            });
            MinecraftServer.getCommandManager().register(c);
            return c;
        });
        addCommand(command, minestomCommand);
    }

    /**
     * Adds the syntax of the given command to the given Minestom command
     *
     * @param command         Lamp command to add
     * @param minestomCommand Minestom command to add into
     */
    @Contract(mutates = "param2")
    private void addCommand(@NotNull ExecutableCommand<A> command, @NotNull Command minestomCommand) {
        Set<String> usedLiterals = new HashSet<>();
        if (command.size() == 1) {
            minestomCommand.setDefaultExecutor((sender, context) -> {
                A actor = actorFactory.create(sender, command.lamp());
                MutableStringStream input = StringStream.createMutable(context.getInput());
                command.execute(actor, input);
            });
        } else {
            List<Argument<?>> arguments = new ArrayList<>();
            if (!command.containsFlags() || command.flagCount() > 4) {
                List<ArgumentColl> addedOptionals = new ArrayList<>();
                for (int i = 1; i < command.nodes().size(); i++) {
                    CommandNode<A> node = command.nodes().get(i);
                    if (node.isLiteral()) {
                        usedLiterals.add(node.name());
                    } else if (usedLiterals.contains(node.name())) {
                        throw new IllegalArgumentException("You cannot have an argument named '" + node.name() + "' because it is used in the literal command path. " +
                                "Pick a different name!");
                    }

                    if (node.isLiteral())
                        arguments.add(toArgument(node));
                    else if (node instanceof ParameterNode<A, ?> parameter) {
                        if (parameter.isSwitch()) {
                            // add the required
                            minestomCommand.addSyntax(generateAction(command), arguments.toArray(Argument[]::new));

                            // add it to the required and then add that.
                            ArgumentColl sw = ofSwitch(parameter);
                            addedOptionals.add(sw);
                        } else if (parameter.isFlag()) {
                            ArgumentColl argumentColl = ofFlag(parameter);
                            if (parameter.isRequired()) {
                                arguments.addAll(argumentColl.arguments());
                            } else {
                                minestomCommand.addSyntax(generateAction(command), arguments.toArray(Argument[]::new));

                                ArgumentColl flag = ofFlag(parameter);
                                addedOptionals.add(flag);
                            }
                        } else {
                            arguments.add(toArgument(node));
                        }
                    }
                }
                minestomCommand.addSyntax(generateAction(command), arguments.toArray(Argument[]::new));
                addOptionalFlagsRecursively(command, minestomCommand, addedOptionals, arguments);
            } else {
                for (int i = 1; i < command.nodes().size(); i++) {
                    CommandNode<A> node = command.nodes().get(i);
                    if (node.isLiteral()) {
                        usedLiterals.add(node.name());
                    } else if (usedLiterals.contains(node.name())) {
                        throw new IllegalArgumentException("You cannot have an argument named '" + node.name() + "' because it is used in the literal command path. " +
                                "Pick a different name!");
                    }

                    if (node.isLiteral())
                        arguments.add(toArgument(node));
                    else if (node instanceof ParameterNode<A, ?> parameter) {
                        if (parameter.isSwitch() || parameter.isFlag()) {
                            continue;
                        }
                        arguments.add(toArgument(node));
                    }
                }

                List<ParameterNode<A, Object>> flags = filter(command.parameters().values(), v -> v.isFlag() || v.isSwitch());
                for (List<ParameterNode<A, Object>> permutation : Permutations.generatePermutations(flags)) {
                    List<List<ArgumentColl>> formsPerParameter = new ArrayList<>(permutation.size());
                    for (ParameterNode<A, Object> parameter : permutation) {
                        if (parameter.isSwitch()) {
                            formsPerParameter.add(ofSwitchForms(parameter));
                        } else if (parameter.isFlag()) {
                            formsPerParameter.add(ofFlagForms(parameter));
                        }
                    }

                    // a switch/flag with a shorthand has two equivalent forms (e.g. --silent
                    // and -s), each of which must be registered as its own Minestom syntax
                    for (List<ArgumentColl> combination : cartesianProduct(formsPerParameter)) {
                        List<Argument<?>> path = new ArrayList<>(arguments);
                        for (ArgumentColl coll : combination) {
                            path.addAll(coll.arguments());
                        }
                        minestomCommand.addSyntax(generateAction(command), path.toArray(Argument[]::new));
                    }
                }
                // we have <= 4 flags, so we create all possible permutations
            }
        }
    }

    private void addOptionalFlagsRecursively(
            @NotNull ExecutableCommand<A> command,
            @NotNull Command minestomCommand,
            @NotNull List<ArgumentColl> addedOptionals,
            @NotNull List<Argument<?>> arguments
    ) {
        for (int i = 0; i < addedOptionals.size(); i++) {
            ArgumentColl o = addedOptionals.get(i);
            List<Argument<?>> after = o.after(arguments);
            minestomCommand.addSyntax(generateAction(command), after.toArray(Argument[]::new));
            for (int j = i + 1; j < addedOptionals.size(); j++) {
                addOptionalFlagsRecursively(
                        command,
                        minestomCommand,
                        addedOptionals.subList(j, addedOptionals.size()),
                        after
                );
            }
        }
    }

    private ArgumentColl ofSwitch(ParameterNode<A, ?> parameter) {
        return new ArgumentColl(
                new ArgumentLiteral(DispatcherSettings.LONG_FORMAT_PREFIX + parameter.switchName())
        );
    }

    private ArgumentColl ofFlag(ParameterNode<A, ?> parameter) {
        return ofFlag(parameter, DispatcherSettings.LONG_FORMAT_PREFIX + parameter.flagName());
    }

    private ArgumentColl ofFlag(ParameterNode<A, ?> parameter, String literal) {
        ArgumentLiteral first = new ArgumentLiteral(literal);
        first.setCallback(createCallback(parameter));
        return new ArgumentColl(
                first,
                toArgument(parameter)
        );
    }

    /**
     * Returns every equivalent form a switch may be written as: its long form
     * (e.g. {@code --silent}), plus its shorthand form (e.g. {@code -s}) if
     * one is defined. Each form must be registered as its own Minestom syntax,
     * since {@link ArgumentLiteral} only matches a single fixed string.
     */
    private List<ArgumentColl> ofSwitchForms(ParameterNode<A, ?> parameter) {
        List<ArgumentColl> forms = new ArrayList<>(2);
        forms.add(ofSwitch(parameter));
        Character shorthand = parameter.shorthand();
        if (shorthand != null) {
            forms.add(new ArgumentColl(
                    new ArgumentLiteral(DispatcherSettings.SHORT_FORMAT_PREFIX + shorthand)
            ));
        }
        return forms;
    }

    /**
     * Returns every equivalent form a flag may be written as. See {@link #ofSwitchForms}.
     */
    private List<ArgumentColl> ofFlagForms(ParameterNode<A, ?> parameter) {
        List<ArgumentColl> forms = new ArrayList<>(2);
        forms.add(ofFlag(parameter));
        Character shorthand = parameter.shorthand();
        if (shorthand != null) {
            forms.add(ofFlag(parameter, DispatcherSettings.SHORT_FORMAT_PREFIX + shorthand));
        }
        return forms;
    }

    /**
     * Computes the cartesian product of the given dimensions, i.e. every
     * combination obtained by picking exactly one element from each dimension.
     */
    private static List<List<ArgumentColl>> cartesianProduct(List<List<ArgumentColl>> dimensions) {
        List<List<ArgumentColl>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (List<ArgumentColl> dimension : dimensions) {
            List<List<ArgumentColl>> next = new ArrayList<>();
            for (List<ArgumentColl> partial : result) {
                for (ArgumentColl choice : dimension) {
                    List<ArgumentColl> combined = new ArrayList<>(partial);
                    combined.add(choice);
                    next.add(combined);
                }
            }
            result = next;
        }
        return result;
    }

    /**
     * Generates a {@link CommandExecutor} that invokes the given command from
     * the Minestom-provided context
     *
     * @param command Command to generate executor for
     * @return The {@link CommandExecutor}
     */
    private @NotNull CommandExecutor generateAction(@NotNull ExecutableCommand<A> command) {
        return (sender, mContext) -> {
            A actor = actorFactory.create(sender, command.lamp());
            MutableExecutionContext<A> context = ExecutionContext.createMutable(command, actor, StringStream.create(mContext.getInput()));
            try {
                readIntoLampContext(context, mContext);
                command.execute(context);
            } catch (Throwable t) {
                command.lamp().handleException(t, ErrorContext.executingFunction(context));
            }
        };
    }

    /**
     * Creates a new {@link Argument} that is equivelant to the given Lamp
     * {@link CommandNode}.
     *
     * @param node The node we need to translate
     * @return The newly created {@link Argument}.
     */
    private @NotNull Argument<?> toArgument(CommandNode<A> node) {
        Argument<?> argument;
        if (node.isLiteral())
            argument = new ArgumentLiteral(node.name());
        else
            argument = parameterToArgument(node.command(), node.requireParameterNode());
        argument.setCallback(createCallback(node));
        return argument;
    }

    /**
     * Creates a new {@link ArgumentCallback} that handles errors for the given
     * node.
     *
     * @param node The node to create for
     * @return The newly created {@link ArgumentCallback}.
     */
    private @NotNull ArgumentCallback createCallback(CommandNode<A> node) {
        return (sender, exception) -> {
            A actor = actorFactory.create(sender, node.lamp());
            StringStream input = StringStream.create(exception.getInput());
            ExecutionContext<A> context = ExecutionContext.create(node.command(), actor, input);
            if (node instanceof LiteralNode<A> literal) {
                node.lamp().handleException(exception, ErrorContext.parsingLiteral(context, literal));
            } else if (node instanceof ParameterNode<A, ?> parameter) {
                node.lamp().handleException(exception, ErrorContext.parsingParameter(context, parameter, input));
            }
        };
    }

    /**
     * Converts the given {@link ParameterNode} to its corresponding
     * {@link Argument} type. This may use the generic {@link ArgumentType#String(String)}
     * or {@link ArgumentType#StringArray(String)} in cases where parameters
     * do not have a specialized {@link Argument}
     *
     * @param command   Command that owns the parameter node
     * @param parameter The parameter to create for
     * @param <T>       The parameter type
     * @return The {@link Argument} that corresponds to the parameter node.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> @NotNull Argument<T> parameterToArgument(
            @NotNull ExecutableCommand<A> command,
            @NotNull ParameterNode<A, T> parameter
    ) {
        Argument<T> argument = (Argument) argumentTypes.type(parameter)
                .orElseGet(() -> adaptFromString(command, parameter));
        if (!argument.getId().equals(parameter.name())) // for safety and consistency
            argument = ArgumentRenamer.rename(argument, parameter.name());
        if (!parameter.suggestions().equals(SuggestionProvider.empty())) {
            argument.setSuggestionCallback(createSuggestionCallback(command, parameter));
        }
        if (parameter.isOptional()) {
            argument.setDefaultValue(commandSender -> {
                A actor = actorFactory.create(commandSender, command.lamp());
                MutableStringStream input = StringStream.createMutable("");
                return parameter.parse(input, ExecutionContext.create(command, actor, input));
            });
        }
        return argument;
    }

    /**
     * Generates a type-safe {@link Argument} that uses an underlying
     * {@link ArgumentType#String(String)} or {@link ArgumentType#StringArray(String)}
     * and delegates to a {@link ParameterType} parser.
     *
     * @param command   The command that owns the parameter
     * @param parameter The parameter node to adapt
     * @param <T>       The parameter type
     * @return The newly created {@link Argument}
     */
    private <T> @NotNull Argument<T> adaptFromString(@NotNull ExecutableCommand<A> command, @NotNull ParameterNode<A, T> parameter) {
        if (parameter.isGreedy())
            return ArgumentType.StringArray(parameter.name())
                    .map((sender, strings) -> {
                        A actor = actorFactory.create(sender, command.lamp());
                        MutableStringStream input = StringStream.createMutable(String.join(" ", strings));
                        return parseOrThrow(command, parameter, actor, input);
                    });
        return ArgumentType.String(parameter.name())
                .map((sender, s) -> {
                    A actor = actorFactory.create(sender, command.lamp());
                    MutableStringStream input = StringStream.createMutable(s);
                    return parseOrThrow(command, parameter, actor, input);
                });
    }

    @Nullable
    private <T> T parseOrThrow(@NotNull ExecutableCommand<A> command, @NotNull ParameterNode<A, T> parameter, A actor, MutableStringStream input) {
        ExecutionContext<A> context = ExecutionContext.create(command, actor, input);
        try {
            return parameter.parse(input, context);
        } catch (Throwable t) {
            if (t.getClass().isAnnotationPresent(ThrowableFromCommand.class))
                throw new ArgumentSyntaxException(t.getMessage(), context.input().source(), 0);
            throw t;
        }
    }

    /**
     * Creates a {@link SuggestionCallback} that wraps the parameter's {@link SuggestionProvider}.
     *
     * @param command   The command that owns the node
     * @param parameter The parameter node
     * @return The newly created {@link SuggestionCallback}.
     */
    private @NotNull SuggestionCallback createSuggestionCallback(
            @NotNull ExecutableCommand<A> command,
            @NotNull ParameterNode<A, ?> parameter
    ) {
        Component tooltipMessage = Component.text(parameter.description() == null ? parameter.name() : parameter.description());
        return (sender, context, suggestion) -> {
            A actor = actorFactory.create(sender, command.lamp());
            MutableExecutionContext<A> executionContext = ExecutionContext.createMutable(
                    command,
                    actor,
                    StringStream.create(context.getInput())
            );
            try {
                readIntoLampContext(executionContext, context);
            } catch (Throwable ignored) {
            }
            Collection<String> suggestions = parameter.suggestions().getSuggestions(executionContext);
            for (String s : suggestions)
                suggestion.addEntry(new SuggestionEntry(s, tooltipMessage));
        };
    }
}

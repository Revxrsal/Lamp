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
package revxrsal.commands.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.node.LiteralNode;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static revxrsal.commands.autocomplete.SuggestionProvider.empty;
import static revxrsal.commands.util.Strings.stripNamespace;

/**
 * A utility that parses all the commands in a {@link Lamp} instance
 * into {@link CommandNode brigadier CommandNodes}
 */
public final class BrigadierAdapter {

    /**
     * Creates a Brigadier {@link CommandNode} based on the given {@link ExecutableCommand}
     *
     * @param command Command to wrap
     * @param adapter The {@link BrigadierConverter} adapter
     * @param <S>     Brigadier sender type
     * @param <A>     Lamp sender type
     * @return The equivalent node
     */
    public static <S, A extends CommandActor> @NotNull LiteralCommandNode<S> createNode(
            @NotNull ExecutableCommand<A> command,
            @NotNull BrigadierConverter<A, S> adapter
    ) {
        LinkedList<ArgumentBuilder<S, ?>> generatedNodes = new LinkedList<>();

        final ArgumentBuilder<S, ?> firstNode = createNode(command.firstNode(), adapter, command.lamp());
        firstNode.requires(createRequirement(command.permission(), adapter, command.lamp()));

        ArgumentBuilder<S, ?> lastNode = firstNode;
        generatedNodes.add(firstNode);

        @Unmodifiable List<revxrsal.commands.node.CommandNode<A>> nodes = command.nodes();
        for (int i = 1; i < nodes.size(); i++) {
            revxrsal.commands.node.CommandNode<A> node = nodes.get(i);
            ArgumentBuilder<S, ?> elementNode = createNode(node, adapter, command.lamp());
            if (node.isLast())
                elementNode.executes(createAction(adapter, command));
            if (node instanceof ParameterNode<?, ?> p && p.isOptional())
                lastNode.executes(createAction(adapter, command));

            generatedNodes.add(elementNode);
            lastNode = elementNode;
        }
        return (LiteralCommandNode<S>) chain(generatedNodes);
    }

    /**
     * Creates a single {@link CommandNode} tree with the elements in the
     * list in the order they are defined.
     *
     * @param list List to read
     * @param <S>  The sender type
     * @return The command node
     * @throws IllegalArgumentException if the list is empty
     */
    private static <S> @NotNull CommandNode<S> chain(@NotNull List<ArgumentBuilder<S, ?>> list) {
        if (list.isEmpty())
            throw new IllegalArgumentException("Cannot chain an empty list.");
        final CommandNode<S> firstNode = list.get(0).build();
        CommandNode<S> lastNode = firstNode;
        for (int i = 1; i < list.size(); i++) {
            ArgumentBuilder<S, ?> builder = list.get(i);
            CommandNode<S> built = builder.build();
            lastNode.addChild(built);
            lastNode = built;
        }
        return firstNode;
    }

    /**
     * Creates a Brigadier {@link CommandNode} based on the given Lamp {@link revxrsal.commands.node.CommandNode}
     *
     * @param node    Node to wrap
     * @param adapter The {@link BrigadierConverter} adapter
     * @param lamp    The {@link Lamp} instance
     * @param <S>     Brigadier sender type
     * @param <A>     Lamp sender type
     * @return The equivalent node
     */
    @SuppressWarnings({"unchecked"})
    public static <S, A extends CommandActor> ArgumentBuilder<S, ?> createNode(
            revxrsal.commands.node.CommandNode<A> node,
            BrigadierConverter<A, S> adapter,
            Lamp<A> lamp
    ) {
        ArgumentBuilder<S, ?> brigadierNode;

        if (node instanceof LiteralNode<A>)
            brigadierNode = literal(node.name());
        else if (node instanceof ParameterNode<A, ?> p) {
            brigadierNode = RequiredArgumentBuilder.<S, Object>argument(node.name(), (ArgumentType) adapter.getArgumentType(p))
                    .suggests(createSuggestionProvider(p, adapter, lamp))
                    .requires(createRequirement(p.permission(), adapter, lamp));
        } else
            throw new IllegalArgumentException("Unsupported node type: " + node);
        return brigadierNode;
    }

    /**
     * Creates a {@link Predicate} that is equivalent to a {@link CommandPermission}
     *
     * @param permission Permission to wrap
     * @param adapter    The {@link BrigadierConverter} adapter
     * @param lamp       The {@link Lamp} instance
     * @param <S>        The Brigadier sender type
     * @param <A>        The Lamp actor type
     * @return The wrapped predicate
     */
    public static <S, A extends CommandActor> @NotNull Predicate<S> createRequirement(
            @NotNull CommandPermission<A> permission,
            @NotNull BrigadierConverter<A, S> adapter,
            @NotNull Lamp<A> lamp
    ) {
        if (permission == CommandPermission.alwaysTrue())
            return x -> true;
        return o -> {
            A actor = adapter.createActor(o, lamp);
            return permission.isExecutableBy(actor);
        };
    }

    /**
     * Returns a Brigadier {@link Command} action that always delegates
     * the execution to the supplied {@link Lamp} instance.
     *
     * @param adapter The {@link BrigadierConverter} adapter
     * @param lamp    The {@link Lamp} instance
     * @param <S>     The Brigadier sender type
     * @param <A>     The Lamp actor type
     * @return The wrapped {@link Command}
     */
    public static <S, A extends CommandActor> @NotNull Command<S> createAction(
            BrigadierConverter<A, S> adapter,
            Lamp<A> lamp
    ) {
        return a -> {
            MutableStringStream input = StringStream.createMutable(a.getInput());
            if (input.peekUnquotedString().contains(":"))
                input = StringStream.createMutable(stripNamespace(a.getInput()));

            A actor = adapter.createActor(a.getSource(), lamp);
            lamp.dispatch(actor, input);
            return Command.SINGLE_SUCCESS;
        };
    }

    /**
     * Returns a Brigadier {@link Command} action that always delegates
     * the execution to the supplied {@link Lamp} instance.
     *
     * @param adapter The {@link BrigadierConverter} adapter
     * @param command The {@link ExecutableCommand} to run
     * @param <S>     The Brigadier sender type
     * @param <A>     The Lamp actor type
     * @return The wrapped {@link Command}
     */
    public static <S, A extends CommandActor> @NotNull Command<S> createAction(
            BrigadierConverter<A, S> adapter,
            ExecutableCommand<A> command
    ) {
        return a -> {
            MutableStringStream input = StringStream.createMutable(a.getInput());
            if (input.peekUnquotedString().contains(":"))
                input = StringStream.createMutable(stripNamespace(a.getInput()));
            A actor = adapter.createActor(a.getSource(), command.lamp());
            command.execute(actor, input);
            return Command.SINGLE_SUCCESS;
        };
    }

    /**
     * Returns a Brigadier {@link SuggestionProvider} action that always delegates
     * the auto-completion to the given {@link ParameterNode}.
     * <p>
     * If the specified parameter has a {@link ParameterType} generated from
     * {@link #toParameterType(ArgumentType)}, the wrapped {@link ArgumentType} will
     * provide suggestions unless explicitly overridden.
     * <p>
     * This may return null if the parameter node's provider equals
     * {@link revxrsal.commands.autocomplete.SuggestionProvider#empty()}, as Brigadier
     * treats such parameters in a more nuanced way.
     *
     * @param adapter The {@link BrigadierConverter} adapter
     * @param lamp    The {@link Lamp} instance
     * @param <S>     The Brigadier sender type
     * @param <A>     The Lamp actor type
     * @return The wrapped {@link Command}
     */
    public static <S, A extends CommandActor> @Nullable SuggestionProvider<S> createSuggestionProvider(
            ParameterNode<A, ?> parameter,
            BrigadierConverter<A, S> adapter,
            Lamp<A> lamp
    ) {
        if (parameter.suggestions().equals(empty())) {
            if (parameter.parameterType() instanceof BrigadierParameterType<?, ?> brigadierParameterType) {
                return brigadierParameterType.argumentType::listSuggestions;
            }
            return null;
        }
        String tooltipMessage = parameter.description() == null ? parameter.name() : parameter.description();
        return (context, builder) -> {
            A actor = adapter.createActor(context.getSource(), lamp);
            Message tooltip = new LiteralMessage(tooltipMessage);
            String input = context.getInput();
            MutableStringStream stream = StringStream.createMutable(
                    input.startsWith("/") ? input.substring(1) : input
            );
            if (stream.peekUnquotedString().indexOf(':') != -1)
                stream = StringStream.createMutable(stripNamespace(input));
            lamp.autoCompleter().complete(actor, stream)
                    .stream()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .distinct()
                    .forEach(c -> builder.suggest(c, tooltip));
            return builder.buildFuture();
        };
    }

    /**
     * Wraps the given {@link ArgumentType} into a {@link ParameterType}.
     * <p>
     * Note that this will not give the same suggestions as the given type.
     *
     * @param argumentType Argument type to wrap
     * @param <A>          The actor type
     * @param <T>          The parameter type
     * @return The parameter node
     */
    public static <A extends CommandActor, T> @NotNull ParameterType<A, T> toParameterType(@NotNull ArgumentType<T> argumentType) {
        return new BrigadierParameterType<>(argumentType);
    }

    /**
     * A {@link ParameterType} that wraps a Brigadier {@link ArgumentType}
     *
     * @param argumentType The argument to wrap
     * @param <A>          The actor type
     * @param <T>          The parameter type
     */
    private record BrigadierParameterType<A extends CommandActor, T>(
            ArgumentType<T> argumentType
    ) implements ParameterType<A, T> {

        @Override public boolean isGreedy() {
            if (argumentType instanceof StringArgumentType sat) {
                return sat.getType() == StringArgumentType.StringType.GREEDY_PHRASE;
            }
            return false;
        }

        @SneakyThrows
        @Override public T parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<A> context) {
            StringReader reader = new StringReader(input.source());
            reader.setCursor(input.position());
            T result = argumentType.parse(reader);
            input.setPosition(reader.getCursor());
            return result;
        }
    }

}

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
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static revxrsal.commands.node.DispatcherSettings.LONG_FORMAT_PREFIX;
import static revxrsal.commands.util.Strings.stripNamespace;

public final class BrigadierParser<S, A extends CommandActor> {

    private final @NotNull BrigadierConverter<A, S> converter;

    public BrigadierParser(@NotNull BrigadierConverter<A, S> converter) {
        this.converter = converter;
    }

    /**
     * Creates a Brigadier {@link CommandNode} based on the given {@link ExecutableCommand}
     *
     * @param command Command to wrap
     * @return The equivalent node
     */
    public @NotNull LiteralCommandNode<S> createNode(@NotNull ExecutableCommand<A> command) {
        final BNode<S> firstNode = BNode.literal(command.firstNode().name());
        firstNode.requires(createRequirement(command.permission(), command.lamp()));

        BNode<S> lastNode = firstNode;

        @Unmodifiable List<revxrsal.commands.node.CommandNode<A>> nodes = command.nodes();
        for (int i = 1; i < nodes.size(); i++) {
            revxrsal.commands.node.CommandNode<A> node = nodes.get(i);
            BNode<S> elementNode;
            if (node.isLiteral()) {
                elementNode = BNode.literal(node.name());
            } else if (node instanceof ParameterNode<A, ?> parameter) {
                if (parameter.isSwitch() || parameter.isFlag())
                    break;
                elementNode = BNode.of(ofParameter(parameter));

                if (parameter.isOptional())
                    lastNode.executes(createAction(command));

            } else {
                throw new UnsupportedOperationException(); // for completeness
            }
            lastNode.then(elementNode);
            lastNode = elementNode;
        }
        if (!command.containsFlags()) {
            lastNode.executes(createAction(command));
            return (LiteralCommandNode<S>) firstNode.asBrigadierNode();
        }
        List<BNode<S>> addOptionalsTo = new ArrayList<>();
        for (ParameterNode<A, Object> parameter : command.parameters().values()) {
            if (!parameter.isFlag() && !parameter.isSwitch()) {
                continue;
            }
            if (parameter.isSwitch()) {
                BNode<S> ofSwitch = ofSwitch(parameter);
                if (!addOptionalsTo.isEmpty()) {
                    addOptionalsTo.forEach(genNode -> {
                        genNode.then(ofSwitch);
                        genNode.executes(createAction(command));
                    });
                } else {
                    lastNode.then(ofSwitch);
                    lastNode.executes(createAction(command));
                }
                addOptionalsTo.add(ofSwitch);
            } else if (parameter.isFlag()) {
                BNode<S> ofFlag = ofFlag(parameter);
                addOptionalsTo.forEach(genNode -> genNode.then(ofFlag));
                if (parameter.isOptional()) {
                    if (addOptionalsTo.isEmpty())
                        lastNode.executes(createAction(command));
                    else
                        addOptionalsTo.forEach(genNode -> genNode.executes(createAction(command)));
                }
                lastNode.then(ofFlag);
                addOptionalsTo.add(ofFlag.nextChild());
                if (parameter.isRequired()) {
                    lastNode = ofFlag.nextChild();
                }
            }
        }
        if (!addOptionalsTo.isEmpty())
            addOptionalsTo.forEach(genNode -> genNode.executes(createAction(command)));
        else
            lastNode.executes(createAction(command));
        return (LiteralCommandNode<S>) firstNode.asBrigadierNode();
    }

    private @NotNull <T> ArgumentCommandNode<S, T> ofParameter(ParameterNode<A, T> parameter) {
        @SuppressWarnings("unchecked")
        RequiredArgumentBuilder<S, T> builder = (RequiredArgumentBuilder<S, T>) RequiredArgumentBuilder
                .argument(parameter.name(), converter.getArgumentType(parameter));
        return builder
                .suggests(createSuggestionProvider(parameter))
                .requires(createRequirement(parameter.permission(), parameter.lamp()))
                .build();
    }

    private <T> BNode<S> ofFlag(ParameterNode<A, T> parameter) {
        ArgumentCommandNode<S, T> ofParameter = ofParameter(parameter);
        return BNode.<S>literal(LONG_FORMAT_PREFIX + parameter.flagName()).then(ofParameter);
    }

    private @NotNull BNode<S> ofSwitch(
            @NotNull ParameterNode<A, ?> parameter
    ) {
        return BNode.<S>literal(LONG_FORMAT_PREFIX + parameter.switchName())
                .requires(createRequirement(parameter.permission(), parameter.lamp()))
                .executes(createAction(parameter.lamp()));
    }

    /**
     * Creates a {@link Predicate} that is equivalent to a {@link CommandPermission}
     *
     * @param permission Permission to wrap
     * @param lamp       The {@link Lamp} instance
     * @return The wrapped predicate
     */
    public @NotNull Predicate<S> createRequirement(
            @NotNull CommandPermission<A> permission,
            @NotNull Lamp<A> lamp
    ) {
        if (permission == CommandPermission.alwaysTrue())
            return x -> true;
        return o -> {
            A actor = converter.createActor(o, lamp);
            return permission.isExecutableBy(actor);
        };
    }

    /**
     * Returns a Brigadier {@link Command} action that always delegates
     * the execution to the supplied {@link Lamp} instance.
     *
     * @param lamp The {@link Lamp} instance
     * @return The wrapped {@link Command}
     */
    public @NotNull Command<S> createAction(@NotNull Lamp<A> lamp) {
        return a -> {
            MutableStringStream input = StringStream.createMutable(a.getInput());
            if (input.peekUnquotedString().contains(":"))
                input = StringStream.createMutable(stripNamespace(a.getInput()));

            A actor = converter.createActor(a.getSource(), lamp);
            lamp.dispatch(actor, input);
            return Command.SINGLE_SUCCESS;
        };
    }

    /**
     * Returns a Brigadier {@link Command} action that always delegates
     * the execution to the supplied {@link Lamp} instance.
     *
     * @param command The {@link ExecutableCommand} to run
     * @return The wrapped {@link Command}
     */
    public @NotNull Command<S> createAction(@NotNull ExecutableCommand<A> command) {
        return a -> {
            MutableStringStream input = StringStream.createMutable(a.getInput());
            if (input.peekUnquotedString().contains(":"))
                input = StringStream.createMutable(stripNamespace(a.getInput()));
            A actor = converter.createActor(a.getSource(), command.lamp());
            command.execute(actor, input);
            return Command.SINGLE_SUCCESS;
        };
    }

    /**
     * Returns a Brigadier {@link SuggestionProvider} action that always delegates
     * the auto-completion to the given {@link ParameterNode}.
     * <p>
     * This may return null if the parameter node's provider equals
     * {@link revxrsal.commands.autocomplete.SuggestionProvider#empty()}, as Brigadier
     * treats such parameters in a more nuanced way.
     *
     * @return The wrapped {@link Command}
     */
    public @Nullable SuggestionProvider<S> createSuggestionProvider(
            @NotNull ParameterNode<A, ?> parameter
    ) {
        return BrigadierAdapter.createSuggestionProvider(parameter, converter);
    }

}

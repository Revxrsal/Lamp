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
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.Potential;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import static revxrsal.commands.autocomplete.SuggestionProvider.empty;
import static revxrsal.commands.util.Strings.stripNamespace;

/**
 * A utility that parses all the commands in a {@link Lamp} instance
 * into {@link CommandNode brigadier CommandNodes}
 */
public final class BrigadierAdapter {

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
     * @param converter The {@link BrigadierConverter}
     * @param <S>       The Brigadier sender type
     * @param <A>       The Lamp actor type
     * @return The wrapped {@link Command}
     */
    public static <S, A extends CommandActor> @Nullable SuggestionProvider<S> createSuggestionProvider(
            ParameterNode<A, ?> parameter,
            BrigadierConverter<A, S> converter
    ) {
        if (parameter.suggestions().equals(empty())) {
            if (parameter.parameterType() instanceof BrigadierParameterType<?, ?> brigadierParameterType) {
                return brigadierParameterType.argumentType::listSuggestions;
            }
            return null;
        }
        String tooltipMessage = parameter.description() == null ? parameter.name() : parameter.description();
        return (context, builder) -> {
            A actor = converter.createActor(context.getSource(), parameter.lamp());
            Message tooltip = new LiteralMessage(tooltipMessage);
            String input = context.getInput();
            MutableStringStream stream = StringStream.createMutable(
                    input.startsWith("/") ? input.substring(1) : input
            );
            if (stream.peekUnquotedString().indexOf(':') != -1)
                stream = StringStream.createMutable(stripNamespace(input));

            Potential<A> test = parameter.command().test(actor, stream.toMutableCopy());
            parameter.suggestions().getSuggestions(test.context())
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

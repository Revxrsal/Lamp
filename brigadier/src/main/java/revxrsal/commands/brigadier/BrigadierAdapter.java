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
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.*;
import com.mojang.brigadier.tree.CommandNode;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.autocomplete.AsyncSuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.Potential;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
        revxrsal.commands.autocomplete.SuggestionProvider<A> suggestions = parameter.suggestions();
        if (suggestions.equals(empty())) {
            if (parameter.parameterType() instanceof BrigadierParameterType<?, ?>) {
                BrigadierParameterType<?, ?> brigadierParameterType = (BrigadierParameterType<?, ?>) parameter.parameterType();
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
            if (suggestions instanceof AsyncSuggestionProvider<?>) {
                //noinspection unchecked
                return provideAsyncCompletions((AsyncSuggestionProvider<A>) suggestions, builder, test.context(), tooltip);
            }

            List<@NotNull Suggestion> values = suggestions.getSuggestions(test.context())
                    .stream()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .distinct()
                    .map(s -> toSuggestion(s, builder, tooltip))
                    .collect(Collectors.toList());
            return CompletableFuture.completedFuture(Suggestions.create(builder.getInput(), values));
        };
    }

    public static <A extends CommandActor> @NotNull CompletableFuture<Suggestions> provideAsyncCompletions(
            @NotNull AsyncSuggestionProvider<A> suggestions,
            @NotNull SuggestionsBuilder builder,
            @NotNull ExecutionContext<A> context,
            @Nullable Message tooltip
    ) {
        CompletableFuture<Collection<String>> completions = suggestions
                .getSuggestionsAsync(context);
        return completions.thenApply(strings -> {
            return Suggestions.create(builder.getInput(),
                    strings.stream()
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .distinct()
                            .map(v -> toSuggestion(v, builder, tooltip))
                            .collect(Collectors.toList()));
        });
    }

    private static @NotNull Suggestion toSuggestion(
            @NotNull String value,
            @NotNull SuggestionsBuilder builder,
            @Nullable Message tooltip
    ) {
        try {
            int intValue = Integer.parseInt(value);
            return new IntegerSuggestion(
                    StringRange.between(builder.getStart(), builder.getInput().length()), intValue, tooltip
            );
        } catch (NumberFormatException e) {
            return new Suggestion(
                    StringRange.between(builder.getStart(), builder.getInput().length()), value, tooltip
            );
        }
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
     */
    private static final class BrigadierParameterType<A extends CommandActor, T> implements ParameterType<A, T> {
        private final ArgumentType<T> argumentType;

        /**
         * @param argumentType The argument to wrap
         */
        private BrigadierParameterType(
                ArgumentType<T> argumentType
        ) {this.argumentType = argumentType;}

        @Override public boolean isGreedy() {
            if (argumentType instanceof StringArgumentType) {
                StringArgumentType sat = (StringArgumentType) argumentType;
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

        public ArgumentType<T> argumentType() {return argumentType;}

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            //noinspection unchecked
            BrigadierParameterType<A, ?> that = (BrigadierParameterType<A, ?>) obj;
            return Objects.equals(this.argumentType, that.argumentType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(argumentType);
        }

        @Override
        public String toString() {
            return "BrigadierParameterType[" +
                    "argumentType=" + argumentType + ']';
        }

    }
}

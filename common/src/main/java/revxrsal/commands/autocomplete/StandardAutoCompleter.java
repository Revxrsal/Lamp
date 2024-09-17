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
package revxrsal.commands.autocomplete;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.*;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.*;

import static revxrsal.commands.node.DispatcherSettings.LONG_FORMAT_PREFIX;
import static revxrsal.commands.node.DispatcherSettings.SHORT_FORMAT_PREFIX;
import static revxrsal.commands.util.Collections.filter;
import static revxrsal.commands.util.Collections.map;

/**
 * A basic implementation of {@link AutoCompleter} that respects secret
 * commands or commands that are not accessible by the user.
 * <p>
 * Create using {@link AutoCompleter#create(Lamp)}
 *
 * @param <A> The actor type
 */
final class StandardAutoCompleter<A extends CommandActor> implements AutoCompleter<A> {

    private final Lamp<A> lamp;

    public StandardAutoCompleter(Lamp<A> lamp) {
        this.lamp = lamp;
    }

    private static @NotNull List<String> filterWithSpaces(Collection<String> suggestions, String consumed) {
        return suggestions
                .stream()
                .filter(suggestion -> startsWithIgnoreCase(suggestion, consumed))
                .map(s -> getRemainingContent(s, consumed))
                .toList();
    }

    private static boolean startsWithIgnoreCase(String a, String b) {
        return a.toLowerCase().startsWith(b.toLowerCase());
    }

    public static String getRemainingContent(String suggestion, String consumed) {
        // Find the index where they match until
        int matchIndex = consumed.length();

        // Find the first space after the matching part
        int spaceIndex = suggestion.lastIndexOf(' ', matchIndex - 1);

        // Return the content after the first space
        return suggestion.substring(spaceIndex + 1);
    }

    @Override
    public @NotNull List<String> complete(@NotNull A actor, @NotNull String input) {
        return complete(actor, StringStream.create(input));
    }

    @Override
    public @NotNull List<String> complete(@NotNull A actor, @NotNull StringStream input) {
        List<String> suggestions = new ArrayList<>();
        if (input.isEmpty())
            return Collections.emptyList();
        String firstWord = input.peekUnquotedString();

        for (ExecutableCommand<A> possible : lamp.registry().commands()) {
            if (possible.isSecret())
                continue;
            if (!possible.firstNode().name().startsWith(firstWord))
                continue;
            if (!possible.permission().isExecutableBy(actor))
                continue;
            suggestions.addAll(complete(possible, input.toMutableCopy(), actor));
        }

        return suggestions;
    }

    private List<String> complete(ExecutableCommand<A> possible, MutableStringStream input, A actor) {
        MutableExecutionContext<A> context = ExecutionContext.createMutable(possible, actor, input.toImmutableCopy());
        List<String> withoutFlags = completeWithoutFlags(possible, input, actor, context);
        if (!possible.containsFlags())
            return withoutFlags;
        List<ParameterNode<A, Object>> flags = filter(possible.parameters().values(), c -> c.isFlag() || c.isSwitch());
        while (input.hasRemaining()) {
            if (input.peek() == ' ')
                input.moveForward();
            String next = input.peekUnquotedString();
            if (next.startsWith(LONG_FORMAT_PREFIX)) {
                String flagName = next.substring(LONG_FORMAT_PREFIX.length());
                @Nullable ParameterNode<A, Object> parameter = removeParameterNamed(flags, flagName);
                input.readUnquotedString();
                if (input.hasFinished())
                    return List.of();
                if (input.hasRemaining() && input.peek() == ' ') {
                    input.moveForward();
                }
                if (input.hasFinished() && parameter != null) {
                    return List.copyOf(parameter.suggestions().getSuggestions(context));
                } else {
                    if (parameter != null) {
                        tryParseFlag(parameter, input, context);
                        if (input.hasFinished() || input.peek() != ' ') {
                            return List.of();
                        }
                    }
                    continue;
                }
            } else if (next.startsWith(SHORT_FORMAT_PREFIX)) {
                String shortenedString = next.substring(SHORT_FORMAT_PREFIX.length());
                char[] spec = shortenedString.toCharArray();
                input.readUnquotedString();
                for (char flag : spec) {
                    @Nullable ParameterNode<A, Object> parameter = removeParameterWithShorthand(flags, flag);
                    if (parameter == null)
                        continue;
                    tryParseFlag(parameter, input, context);
                    if (input.hasRemaining() && input.peek() == ' ') {
                        input.moveForward();
                        return List.copyOf(parameter.suggestions().getSuggestions(context));
                    } else if (input.hasFinished()) {
                        return flags.stream().map(f -> {
                                    if (f.shorthand() != null) {
                                        String result = SHORT_FORMAT_PREFIX + shortenedString + f.shorthand();
                                        return f.isFlag() ? result + ' ' : result;
                                    }
                                    return null;
                                }).filter(Objects::nonNull)
                                .toList();
                    }
                }
            } else {
                break;
            }
        }
        return map(flags, c -> LONG_FORMAT_PREFIX + (c.isSwitch() ? c.switchName() : c.flagName()));
    }

    private void tryParseFlag(@NotNull ParameterNode<A, Object> parameter, MutableStringStream input, MutableExecutionContext<A> context) {
        if (parameter.isSwitch()) {
            context.addResolvedArgument(parameter.name(), true);
        } else {
            try {
                input.moveForward();
                if (parameter.isSwitch()) {
                    context.addResolvedArgument(parameter.name(), true);
                    return;
                }
                Object value = parameter.parse(input, context);
                context.addResolvedArgument(parameter.name(), value);
            } catch (Throwable ignored) {
                input.readUnquotedString();
            }
        }
    }

    private @NotNull List<String> completeWithoutFlags(
            ExecutableCommand<A> possible,
            MutableStringStream input,
            A actor,
            MutableExecutionContext<A> context
    ) {
        for (CommandNode<A> child : possible.nodes()) {
            if (child instanceof ParameterNode<A, ?> parameter) {
                if (parameter.isFlag() || parameter.isSwitch())
                    break;
            }
            if (input.remaining() == 1 && input.peek() == ' ') {
                input.moveForward();
                return promptWith(child, actor, context, input);
            }

            if (child instanceof LiteralNode<A> l) {
                String nextWord = input.readUnquotedString();
                if (input.hasFinished()) {
                    if (l.name().startsWith(nextWord)) {
                        // complete it for the user :)
                        return List.of(l.name());
                    } else {
                        // the user inputted a command that isn't ours. dismiss the operation
                        return List.of();
                    }
                } else {
                    if (!l.name().equalsIgnoreCase(nextWord)) {
                        // the user inputted a command that isn't ours. dismiss the operation
                        return List.of();
                    }
                    if (input.canRead(1) && input.peek() == ' ') {
                        // our literal is just fine. move to the next node
                        input.moveForward();
                        continue;
                    }
                }
            } else if (child instanceof ParameterNode<A, ?> parameter) {
                int posBeforeParsing = input.position();
                if (!parameter.permission().isExecutableBy(actor))
                    return List.of();
                try {
                    Object value = parameter.parse(input, context);
                    context.addResolvedArgument(parameter.name(), value);
                    if (input.hasFinished()) {
                        input.setPosition(posBeforeParsing);
                        String consumed = input.peekRemaining();
                        // user inputted something valid, but we still have some
                        // suggestions. throw it at them
                        if (consumed.contains(" ")) {
                            return filterWithSpaces(parameter.complete(actor, input, context), consumed);
                        }
                        return filter(parameter.complete(actor, input, context), s -> startsWithIgnoreCase(s, consumed));
                    } else if (input.peek() == ' ') {
                        input.moveForward();
                    }
                } catch (Throwable e) {
                    // user inputted invalid input. what do we do here?
                    // 1. restore the stream to its previous state
                    // 2. see what we consumed
                    // 2.1. if suggestion does not contain spaces, we're cool. just
                    //      give the same suggestions
                    // 2.2. if suggestion does contain spaces, pick the part after
                    //      the space
                    int finishedAt = input.position();
                    input.setPosition(posBeforeParsing);
                    String consumed = input.peek(finishedAt - posBeforeParsing);
                    if (consumed.contains(" ")) {
                        return filterWithSpaces(parameter.complete(actor, input, context), consumed);
                    }
                    return filter(parameter.complete(actor, input, context), s -> startsWithIgnoreCase(s, consumed));
                }
            }
        }
        return List.of();
    }

    private @NotNull List<String> promptWith(CommandNode<A> child, A
            actor, ExecutionContext<A> context, StringStream input) {
        if (child instanceof LiteralNode<A> l)
            return List.of(l.name());
        else if (child instanceof ParameterNode<A, ?> p)
            return List.copyOf(p.complete(actor, input, context));
        else
            return List.of();
    }

    private @Nullable ParameterNode<A, Object> removeParameterWithShorthand
            (List<ParameterNode<A, Object>> parametersLeft, char c) {
        for (Iterator<ParameterNode<A, Object>> iterator = parametersLeft.iterator(); iterator.hasNext(); ) {
            ParameterNode<A, Object> value = iterator.next();
            Character shorthand = value.shorthand();
            if (shorthand != null && shorthand == c) {
                iterator.remove();
                return value;
            }
        }
        return null;
    }

    private @Nullable ParameterNode<A, Object> removeParameterNamed
            (List<ParameterNode<A, Object>> parametersLeft, String name) {
        for (Iterator<ParameterNode<A, Object>> iterator = parametersLeft.iterator(); iterator.hasNext(); ) {
            ParameterNode<A, Object> value = iterator.next();
            if (value.isFlag() && Objects.equals(value.flagName(), name)) {
                iterator.remove();
                return value;
            }
            if (value.isSwitch() && Objects.equals(value.switchName(), name)) {
                iterator.remove();
                return value;
            }
        }
        return null;
    }

}

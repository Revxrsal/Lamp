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
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.CommandNode;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.node.LiteralNode;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.node.parser.BasicExecutionContext;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static revxrsal.commands.util.Collections.filter;

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

        for (ExecutableCommand<A> possible : lamp.registry().children()) {
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
        BasicExecutionContext<A> context = new BasicExecutionContext<>(lamp, possible, actor);
        for (CommandNode<A> child : possible.nodes()) {
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
                        // our parameter is just fine. move to the next node
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
                    lamp.validate(context.actor(), value, (ParameterNode<A, Object>) parameter);
                    context.addResoledArgument(parameter.name(), value);
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

    private @NotNull List<String> promptWith(CommandNode<A> child, A actor, ExecutionContext<A> context, StringStream input) {
        if (child instanceof LiteralNode<A> l)
            return List.of(l.name());
        else if (child instanceof ParameterNode<A, ?> p)
            return p.type().defaultSuggestions(input, actor, context);
        else
            return List.of();
    }
}

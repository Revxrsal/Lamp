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
import revxrsal.commands.node.parser.BaseCommandRegistry;
import revxrsal.commands.node.parser.LiteralTree;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.*;

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
        if (input.isEmpty())
            return Collections.emptyList();
        LiteralTree.Match<A> match = literalTree().match(input, false);
        Set<String> suggestions = new LinkedHashSet<>(match.literalCompletions(command -> isVisibleTo(command, actor)));
        for (ExecutableCommand<A> possible : match.candidates()) {
            if (isVisibleTo(possible, actor))
                suggestions.addAll(complete(possible, input.toMutableCopy(), actor));
        }
        return new ArrayList<>(suggestions);
    }

    private LiteralTree<A> literalTree() {
        return ((BaseCommandRegistry<A>) lamp.registry()).literalTree();
    }

    private static <A extends CommandActor> boolean isVisibleTo(ExecutableCommand<A> command, A actor) {
        return !command.isSecret() && command.permission().isExecutableBy(actor);
    }

    private List<String> complete(ExecutableCommand<A> possible, MutableStringStream input, A actor) {
        SingleCommandCompleter<A> commandCompleter = new SingleCommandCompleter<>(actor, possible, input);
        commandCompleter.complete();
        return commandCompleter.suggestions();
    }
}

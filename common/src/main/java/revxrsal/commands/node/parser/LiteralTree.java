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
package revxrsal.commands.node.parser;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.*;
import java.util.function.Predicate;

/**
 * Indexes commands by the literals they start with, so that dispatching and
 * auto-completion only look at the commands that can match the input.
 * <p>
 * A command is stored under its leading literals: {@code island home <name>} lives
 * at {@code island -> home}. Everything from the first argument onwards is matched
 * by the command itself. Commands with flags are stored under their first literal,
 * because flags may appear between literals.
 */
@ApiStatus.Internal
public final class LiteralTree<A extends CommandActor> {

    private final Node<A> root = new Node<>("");

    /**
     * Inserts the command into the list, after all commands that rank before or
     * equal to it. Unlike sorting, this cannot fail on inconsistent comparators.
     */
    static <A extends CommandActor> void insertSorted(List<ExecutableCommand<A>> list, ExecutableCommand<A> command) {
        int index = list.size();
        while (index > 0 && list.get(index - 1).compareTo(command) > 0)
            index--;
        list.add(index, command);
    }

    private static int depthOf(ExecutableCommand<?> command) {
        if (command.containsFlags())
            return 1;
        int depth = 0;
        while (depth < command.size() && command.nodes().get(depth).isLiteral())
            depth++;
        return depth;
    }

    private static String key(String literal) {
        return literal.toLowerCase(Locale.ROOT);
    }

    void add(@NotNull ExecutableCommand<A> command) {
        Node<A> node = root;
        int depth = depthOf(command);
        for (int i = 0; i < depth; i++) {
            String name = command.nodes().get(i).name();
            node = node.children.computeIfAbsent(key(name), k -> new Node<>(name));
        }
        insertSorted(node.commands, command);
    }

    void clear() {
        root.children.clear();
    }

    /**
     * Follows the input through the tree, one literal at a time, until a word
     * matches no literal.
     *
     * @param input             The input
     * @param lastWordComplete  Whether a word at the very end of the input is complete.
     *                          It is not while auto-completing, as the user is still typing it.
     * @return The commands that may match, and where the input stopped matching
     */
    public @NotNull Match<A> match(@NotNull StringStream input, boolean lastWordComplete) {
        MutableStringStream stream = input.toMutableCopy();
        List<ExecutableCommand<A>> candidates = new ArrayList<>();
        Node<A> node = root;
        int depth = 0;
        while (true) {
            stream.skipWhitespace();
            int position = stream.position();
            String word = stream.readUnquotedString();
            boolean endsWord = stream.hasFinished() ? lastWordComplete : stream.peek() == ' ';
            Node<A> child = endsWord ? node.children.get(key(word)) : null;
            if (child == null)
                return new Match<>(candidates, node, depth, word, position, stream.hasFinished());
            node = child;
            depth++;
            for (ExecutableCommand<A> command : node.commands)
                insertSorted(candidates, command);
        }
    }

    private static final class Node<A extends CommandActor> {
        private final String name;
        private final Map<String, Node<A>> children = new LinkedHashMap<>(); // keeps suggestions in registration order
        private final List<ExecutableCommand<A>> commands = new ArrayList<>();

        private Node(String name) {
            this.name = name;
        }

        private void collectCommands(List<ExecutableCommand<A>> into) {
            into.addAll(commands);
            for (Node<A> child : children.values())
                child.collectCommands(into);
        }

        private boolean anyCommand(Predicate<ExecutableCommand<A>> predicate) {
            for (ExecutableCommand<A> command : commands) {
                if (predicate.test(command))
                    return true;
            }
            for (Node<A> child : children.values()) {
                if (child.anyCommand(predicate))
                    return true;
            }
            return false;
        }
    }

    public static final class Match<A extends CommandActor> {

        private final List<ExecutableCommand<A>> candidates;
        private final Node<A> deepest;
        private final int depth;
        private final String mismatch;
        private final int mismatchPosition;
        private final boolean mismatchEndsInput;

        private Match(List<ExecutableCommand<A>> candidates, Node<A> deepest, int depth,
                      String mismatch, int mismatchPosition, boolean mismatchEndsInput) {
            this.candidates = candidates;
            this.deepest = deepest;
            this.depth = depth;
            this.mismatch = mismatch;
            this.mismatchPosition = mismatchPosition;
            this.mismatchEndsInput = mismatchEndsInput;
        }

        /**
         * Returns the commands that may match the input, most specific first
         */
        public @NotNull List<ExecutableCommand<A>> candidates() {
            return candidates;
        }

        /**
         * Returns the number of literals matched. 0 means the first word is not a command.
         */
        int depth() {
            return depth;
        }

        /**
         * Returns the word that matched no literal
         */
        String mismatch() {
            return mismatch;
        }

        /**
         * Returns the position of {@link #mismatch()} in the input
         */
        int mismatchPosition() {
            return mismatchPosition;
        }

        /**
         * Returns the commands that were skipped because their literal at
         * {@link #depth()} is not {@link #mismatch()}.
         */
        List<ExecutableCommand<A>> mismatched() {
            List<ExecutableCommand<A>> commands = new ArrayList<>();
            for (Node<A> child : deepest.children.values())
                child.collectCommands(commands);
            return commands;
        }

        /**
         * Returns the literals that complete the last word of the input, ignoring
         * case. A literal is only included if one of its commands is visible.
         *
         * @param visible Tests whether a command may be suggested
         * @return The literals
         */
        public @NotNull List<String> literalCompletions(@NotNull Predicate<ExecutableCommand<A>> visible) {
            if (!mismatchEndsInput)
                return Collections.emptyList();
            List<String> completions = new ArrayList<>();
            for (Node<A> child : deepest.children.values()) {
                if (child.name.regionMatches(true, 0, mismatch, 0, mismatch.length()) && child.anyCommand(visible))
                    completions.add(child.name);
            }
            return completions;
        }
    }
}

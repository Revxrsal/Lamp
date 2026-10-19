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

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.*;

/**
 * Indexes commands by the literals they start with, so that dispatching only
 * tests the commands that can match the input.
 * <p>
 * A command is stored under its leading literals: {@code island home <name>} lives
 * at {@code island -> home}. Everything from the first argument onwards is matched
 * by the command itself. Commands with flags are stored under their first literal,
 * because flags may appear between literals.
 */
final class LiteralTree<A extends CommandActor> {

    private final Node<A> root = new Node<>();

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

    public void add(@NotNull ExecutableCommand<A> command) {
        Node<A> node = root;
        int depth = depthOf(command);
        for (int i = 0; i < depth; i++)
            node = node.children.computeIfAbsent(key(command.nodes().get(i).name()), k -> new Node<>());
        insertSorted(node.commands, command);
    }

    public void clear() {
        root.children.clear();
    }

    /**
     * Follows the input through the tree, one literal at a time, until a word
     * matches no literal.
     *
     * @param input The input
     * @return The commands that may match, and where the input stopped matching
     */
    public @NotNull Match<A> match(@NotNull StringStream input) {
        MutableStringStream stream = input.toMutableCopy();
        List<ExecutableCommand<A>> candidates = new ArrayList<>();
        Node<A> node = root;
        int depth = 0;
        while (true) {
            stream.skipWhitespace();
            int position = stream.position();
            String word = stream.readUnquotedString();
            Node<A> child = stream.hasFinished() || stream.peek() == ' ' ? node.children.get(key(word)) : null;
            if (child == null)
                return new Match<>(candidates, node, depth, word, position);
            node = child;
            depth++;
            for (ExecutableCommand<A> command : node.commands)
                insertSorted(candidates, command);
        }
    }

    private static final class Node<A extends CommandActor> {
        private final Map<String, Node<A>> children = new HashMap<>();
        private final List<ExecutableCommand<A>> commands = new ArrayList<>();

        private void collectCommands(List<ExecutableCommand<A>> into) {
            into.addAll(commands);
            for (Node<A> child : children.values())
                child.collectCommands(into);
        }
    }

    static final class Match<A extends CommandActor> {

        /**
         * The commands that may match the input, most specific first
         */
        final List<ExecutableCommand<A>> candidates;

        /**
         * The number of literals matched. 0 means the first word is not a command.
         */
        final int depth;

        /**
         * The word that matched no literal, and its position in the input
         */
        final String mismatch;
        final int mismatchPosition;

        private final Node<A> deepest;

        private Match(List<ExecutableCommand<A>> candidates, Node<A> deepest, int depth, String mismatch, int mismatchPosition) {
            this.candidates = candidates;
            this.deepest = deepest;
            this.depth = depth;
            this.mismatch = mismatch;
            this.mismatchPosition = mismatchPosition;
        }

        /**
         * Returns the commands that were skipped because their literal at
         * {@link #depth} is not {@link #mismatch}.
         */
        List<ExecutableCommand<A>> mismatched() {
            List<ExecutableCommand<A>> commands = new ArrayList<>();
            for (Node<A> child : deepest.children.values())
                child.collectCommands(commands);
            return commands;
        }
    }
}

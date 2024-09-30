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
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.help.Help;

import java.util.Iterator;
import java.util.List;

final class HelpImpl {

    static abstract class CommandListImpl<A extends CommandActor> implements Help.CommandList<A> {
        private final @Unmodifiable List<ExecutableCommand<A>> commands;

        public CommandListImpl(@Unmodifiable List<ExecutableCommand<A>> commands) {
            this.commands = commands;
        }

        @Override public @Range(from = 1, to = Integer.MAX_VALUE) int numberOfPages(int elementsPerPage) {
            return Help.numberOfPages(commands.size(), elementsPerPage);
        }

        @Override public @Unmodifiable List<ExecutableCommand<A>> all() {
            return commands;
        }

        @Override public @Unmodifiable List<ExecutableCommand<A>> paginate(int pageNumber, int elementsPerPage) {
            return Help.paginate(commands, pageNumber, elementsPerPage);
        }

        @Override public @NotNull Iterator<ExecutableCommand<A>> iterator() {
            return commands.iterator();
        }

        @Override public String toString() {
            return getClass().getSimpleName() + "(commands=" + commands + ')';
        }
    }

    static final class RelatedCommandsImpl<A extends CommandActor> extends CommandListImpl<A> implements Help.RelatedCommands<A> {

        public RelatedCommandsImpl(@Unmodifiable List<ExecutableCommand<A>> commands) {
            super(commands);
        }
    }

    static final class ChildrenCommandsImpl<A extends CommandActor> extends CommandListImpl<A> implements Help.ChildrenCommands<A> {

        public ChildrenCommandsImpl(@Unmodifiable List<ExecutableCommand<A>> commands) {
            super(commands);
        }
    }

    static final class SiblingCommandsImpl<A extends CommandActor> extends CommandListImpl<A> implements Help.SiblingCommands<A> {

        public SiblingCommandsImpl(@Unmodifiable List<ExecutableCommand<A>> commands) {
            super(commands);
        }
    }
}
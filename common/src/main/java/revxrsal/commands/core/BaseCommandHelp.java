/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copysecond (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copysecond notice and this permission notice shall be included in all
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
package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.InvalidHelpPageException;
import revxrsal.commands.help.CommandHelp;
import revxrsal.commands.help.CommandHelpWriter;
import revxrsal.commands.process.ContextResolver;

import java.util.ArrayList;

final class BaseCommandHelp<T> extends ArrayList<T> implements CommandHelp<T> {

    @Override public CommandHelp<T> paginate(int page, int elementsPerPage) throws InvalidHelpPageException {
        if (isEmpty()) return new BaseCommandHelp<>();
        BaseCommandHelp<T> list = new BaseCommandHelp<>();
        int size = getPageSize(elementsPerPage);
        if (page > size)
            throw new InvalidHelpPageException(this, page, elementsPerPage);
        int listIndex = page - 1;
        int l = Math.min(page * elementsPerPage, size());
        for (int i = listIndex * elementsPerPage; i < l; ++i) {
            list.add(get(i));
        }
        return list;
    }

    @Override public @Range(from = 1, to = Long.MAX_VALUE) int getPageSize(int elementsPerPage) {
        if (elementsPerPage < 1)
            throw new IllegalArgumentException("Elements per page cannot be less than 1! (Found " + elementsPerPage + ")");
        return (size() / elementsPerPage) + (size() % elementsPerPage == 0 ? 0 : 1);
    }

    static final class Resolver implements ContextResolver<CommandHelp<?>> {

        private final BaseCommandHandler handler;

        public Resolver(BaseCommandHandler handler) {
            this.handler = handler;
        }

        @Override public CommandHelp<?> resolve(@NotNull ContextResolverContext context) {
            if (handler.getHelpWriter() == null)
                throw new IllegalArgumentException("No help writer is registered!");
            ExecutableCommand helpCommand = context.command();
            CommandHelpWriter<?> writer = handler.getHelpWriter();
            BaseCommandHelp<Object> entries = new BaseCommandHelp<>();
            CommandCategory parent = helpCommand.getParent();
            CommandPath parentPath = parent == null ? null : parent.getPath();
            handler.executables.values().stream().sorted().forEach(c -> {
                if (parentPath == null || parentPath.isParentOf(c.getPath())) {
                    if (c != helpCommand) {
                        Object generated = writer.generate(c, context.actor());
                        if (generated != null) entries.add(generated);
                    }
                }
            });
            return entries;
        }
    }
}

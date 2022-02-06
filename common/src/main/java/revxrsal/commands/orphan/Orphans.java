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
package revxrsal.commands.orphan;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.core.CommandPath;
import revxrsal.commands.util.Strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static revxrsal.commands.util.Preconditions.notNull;

/**
 * Represents the entrypoint to creation of {@link OrphanCommand}s.
 * <p>
 * Methods {@link #path(String)}, {@link #path(String...)} have the same exact
 * behavior as {@link Command} annotations.
 *
 * <pre>
 * {@code
 * - @Command("foo bar") -> Orphans.path("foo bar")
 * - @Command("foo", "bar") -> Orphans.path("foo", "bar")
 * - @Command("foo bar", "buzz buff") -> Orphans.path("foo bar", "buzz buff")
 * }
 * </pre>
 * Registering {@link OrphanCommand}s directly into a {@link CommandHandler#register(Object...)}
 * will throw an exception. Orphan commands should be wrapped using this class as follows:
 * <pre>
 * {@code
 * commandHandler.register(Orphans.path("foo").handler(new Foo()));
 * }
 * </pre>
 *
 * @see OrphanCommand
 */
@AllArgsConstructor
public final class Orphans {

    /**
     * Starts the registration of an orphan command. The input for
     * this method has exactly the same behavior as {@link Command}.
     * <p>
     * {@code
     * - @Command("foo bar") -> Orphans.path("foo bar")
     * }
     *
     * @param path The command path. This accepts spaces for commands
     *             with subcategories.
     * @return A builder {@link Orphans}. You must call {@link #handler(OrphanCommand)} after.
     */
    public static Orphans path(@NotNull String path) {
        notNull(path, "path");
        return new Orphans(Collections.singletonList(CommandPath.get(Strings.splitBySpace(path.trim()))));
    }

    /**
     * Starts the registration of an orphan command. The input for
     * this method has exactly the same behavior as {@link Command}.
     * <p>
     * {@code
     * - @Command("foo", "bar") -> Orphans.path("foo", "bar")
     * }
     *
     * @param paths The command paths. These accept spaces for commands
     *              with subcategories.
     * @return A builder {@link Orphans}. You must call {@link #handler(OrphanCommand)} after.
     */
    public static Orphans path(@NotNull String... paths) {
        notNull(paths, "paths");
        return new Orphans(Arrays.stream(paths)
                .map(text -> Strings.splitBySpace(text.trim()))
                .map(CommandPath::get)
                .collect(Collectors.toList()));
    }

    /**
     * Starts the registration of an orphan command.
     *
     * @param path The command path. See {@link CommandPath}.
     * @return A builder {@link Orphans}. You must call {@link #handler(OrphanCommand)} after.
     */
    public static Orphans path(@NotNull CommandPath path) {
        notNull(path, "path");
        return new Orphans(Collections.singletonList(path));
    }

    /**
     * Sets the handler of this orphan command. This can be any class, however it must
     * implement {@link OrphanCommand}.
     *
     * @param handler The command class logic.
     * @return An {@link OrphanRegistry} that can be passed to {@link CommandHandler#register(Object...)}
     * to be registered.
     */
    public OrphanRegistry handler(OrphanCommand handler) {
        notNull(handler, "orphan command");
        return new OrphanRegistry(path, handler);
    }

    private final List<CommandPath> path;

}

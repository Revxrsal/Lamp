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

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Command;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static revxrsal.commands.util.Preconditions.notNull;

/**
 * Represents the entrypoint to creation of {@link OrphanCommand}s.
 * <p>
 * The method {@link #path(String...)} has the same exact behavior
 * as {@link Command} annotations.
 *
 * <pre>
 * {@code
 * - @Command("foo bar") -> Orphans.path("foo bar")
 * - @Command("foo", "bar") -> Orphans.path("foo", "bar")
 * - @Command("foo bar", "buzz buff") -> Orphans.path("foo bar", "buzz buff")
 * }
 * </pre>
 * Registering {@link OrphanCommand}s directly into a {@link Lamp#register(Object)}
 * will throw an exception. Orphan commands should be wrapped using this class as follows:
 * <pre>
 * {@code
 * lamp.register(Orphans.path("foo").handler(new Foo()));
 * }
 * </pre>
 *
 * @see OrphanCommand
 */
public final class Orphans {
    private final List<String> paths;

    /**
     *
     */
    public Orphans(List<String> paths) {this.paths = paths;}

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
        return new Orphans(Arrays.asList(paths));
    }

    /**
     * Sets the handler of this orphan command. This can be any class, however it must
     * implement {@link OrphanCommand}.
     *
     * @param handler The command class logic.
     * @return An {@link OrphanRegistry} that can be passed to {@link Lamp#register(Object)}
     * to be registered.
     */
    public OrphanRegistry handler(OrphanCommand handler) {
        notNull(handler, "orphan command");
        return new OrphanRegistry(paths, handler);
    }

    public List<String> paths() {return paths;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Orphans that = (Orphans) obj;
        return Objects.equals(this.paths, that.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paths);
    }

    @Override
    public String toString() {
        return "Orphans[" +
                "paths=" + paths + ']';
    }

}

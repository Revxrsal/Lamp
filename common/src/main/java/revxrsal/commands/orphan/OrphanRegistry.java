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
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;

import java.util.List;
import java.util.Objects;

/**
 * Represents an orphan command that has finally found its parent path.
 * <p>
 * Instances of this method can be safely passed to {@link Lamp#register(Object)}
 * to be registered.
 * <p>
 * This should be constructed using {@link Orphans}'s methods.
 */
public final class OrphanRegistry {
    private final @NotNull
    @Unmodifiable List<String> paths;
    private final @NotNull OrphanCommand handler;

    /**
     *
     */
    public OrphanRegistry(@NotNull @Unmodifiable List<String> paths, @NotNull OrphanCommand handler) {
        this.paths = paths;
        this.handler = handler;
    }

    public @NotNull @Unmodifiable List<String> paths() {return paths;}

    public @NotNull OrphanCommand handler() {return handler;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        OrphanRegistry that = (OrphanRegistry) obj;
        return Objects.equals(this.paths, that.paths) &&
                Objects.equals(this.handler, that.handler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paths, handler);
    }

    @Override
    public String toString() {
        return "OrphanRegistry[" +
                "paths=" + paths + ", " +
                "handler=" + handler + ']';
    }

}
/*
 * This file is part of sweeper, licensed under the MIT License.
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
package revxrsal.commands;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;

/**
 * Represents a <a href="https://en.wikipedia.org/wiki/Visitor_pattern">visitor</a>
 * for a {@link Lamp} object. This provides a convenient way of performing additional
 * registrations or hooks in a modular fashion.
 * <p>
 * To accept a visitor, use {@link Lamp#accept(LampVisitor)}.
 */
@FunctionalInterface
public interface LampVisitor<A extends CommandActor> {

    /**
     * Returns a {@link LampVisitor} that does nothing
     *
     * @param <A> The actor type
     * @return A visitor that does nothing
     */
    static <A extends CommandActor> @NotNull LampVisitor<A> nothing() {
        return lamp -> {};
    }

    /**
     * Visits the given {@link Lamp}
     *
     * @param lamp The instance to visit.
     */
    void visit(@NotNull Lamp<A> lamp);
}

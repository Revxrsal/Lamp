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
package revxrsal.commands.exception.context;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;

import java.util.Objects;

/**
 * Right now this context stores no information. We can just use
 * a singleton for it
 */
final class UnknownCommandContext<A extends CommandActor> implements ErrorContext.UnknownCommand<A> {
    private final @NotNull A actor;

    /**
     *
     */
    UnknownCommandContext(@NotNull A actor) {this.actor = actor;}

    @Override
    public boolean hasExecutionContext() {
        return false;
    }

    @Override
    public ExecutionContext<A> context() {
        return null;
    }

    @Override
    public @NotNull A actor() {
        return actor;
    }

    @Override
    public @NotNull Lamp<A> lamp() {
        return (Lamp<A>) actor.lamp();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        UnknownCommandContext that = (UnknownCommandContext) obj;
        return Objects.equals(this.actor, that.actor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actor);
    }

    @Override
    public String toString() {
        return "UnknownCommandContext[" +
                "actor=" + actor + ']';
    }

}

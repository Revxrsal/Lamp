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
import revxrsal.commands.node.LiteralNode;

import java.util.Objects;

final class ParsingLiteralContext<A extends CommandActor> implements ErrorContext.ParsingLiteral<A> {
    private final @NotNull ExecutionContext<A> context;
    private final @NotNull LiteralNode<A> literal;

    ParsingLiteralContext(
            @NotNull ExecutionContext<A> context,
            @NotNull LiteralNode<A> literal
    ) {
        this.context = context;
        this.literal = literal;
    }

    @Override
    public @NotNull A actor() {
        return context().actor();
    }

    @Override
    public @NotNull Lamp<A> lamp() {
        return context.lamp();
    }

    @Override public @NotNull ExecutionContext<A> context() {return context;}

    @Override public @NotNull LiteralNode<A> literal() {return literal;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ParsingLiteralContext that = (ParsingLiteralContext) obj;
        return Objects.equals(this.context, that.context) &&
                Objects.equals(this.literal, that.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, literal);
    }

    @Override
    public String toString() {
        return "ParsingLiteralContext[" +
                "context=" + context + ", " +
                "literal=" + literal + ']';
    }

}

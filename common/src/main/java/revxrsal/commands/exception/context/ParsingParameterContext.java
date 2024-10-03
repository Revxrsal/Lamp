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
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.stream.StringStream;

import java.util.Objects;

final class ParsingParameterContext<A extends CommandActor> implements ErrorContext.ParsingParameter<A> {
    private final @NotNull ExecutionContext<A> context;
    private final @NotNull ParameterNode<A, ?> parameter;
    private final @NotNull StringStream input;

    ParsingParameterContext(
            @NotNull ExecutionContext<A> context,
            @NotNull ParameterNode<A, ?> parameter,
            @NotNull StringStream input
    ) {
        this.context = context;
        this.parameter = parameter;
        this.input = input;
    }

    @Override
    public @NotNull A actor() {
        return context.actor();
    }

    @Override
    public @NotNull Lamp<A> lamp() {
        return context.lamp();
    }

    @Override public @NotNull ExecutionContext<A> context() {return context;}

    @Override public @NotNull ParameterNode<A, ?> parameter() {return parameter;}

    public @NotNull StringStream input() {return input;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ParsingParameterContext that = (ParsingParameterContext) obj;
        return Objects.equals(this.context, that.context) &&
                Objects.equals(this.parameter, that.parameter) &&
                Objects.equals(this.input, that.input);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, parameter, input);
    }

    @Override
    public String toString() {
        return "ParsingParameterContext[" +
                "context=" + context + ", " +
                "parameter=" + parameter + ", " +
                "input=" + input + ']';
    }

}

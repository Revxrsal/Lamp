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
package revxrsal.commands.parameter;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.stream.MutableStringStream;

/**
 * A basic {@link ParameterType} for parsing {@link String} types
 */
public final class StringParameterType implements ParameterType<CommandActor, String> {

    private static final StringParameterType GREEDY = new StringParameterType(true);
    private static final StringParameterType SINGLE = new StringParameterType(false);

    private final boolean greedy;

    private StringParameterType(boolean greedy) {
        this.greedy = greedy;
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <A extends CommandActor> ParameterType<A, String> greedy() {
        return (ParameterType<A, String>) GREEDY;
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <A extends CommandActor> ParameterType<A, String> single() {
        return (ParameterType<A, String>) SINGLE;
    }

    @Override
    public String parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<CommandActor> context) {
        if (greedy)
            return input.consumeRemaining();
        return input.readString();
    }

    @Override public boolean isGreedy() {
        return greedy;
    }

    @Override
    public @NotNull PrioritySpec parsePriority() {
        return PrioritySpec.lowest();
    }

}

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
package revxrsal.commands.minestom;

import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.minestom.actor.MinestomCommandActor;
import revxrsal.commands.minestom.argument.ArgumentTypes;
import revxrsal.commands.minestom.argument.MinestomArgumentTypes;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

/**
 * This is a stub to tell Lamp that we can parse a certain parameter type.
 * Real argument parsing is done in Minestom's {@link Argument} classes.
 * <p>
 * The {@link #parse(MutableStringStream, ExecutionContext)} should never be called directly, and
 * this {@link ParameterType} should only be used for types that have
 * corresponding {@link Argument arguments} registered in {@link MinestomArgumentTypes}.
 * <p>
 * If you want to use this type, make sure you have the corresponding {@link Argument}
 * registered in {@link ArgumentTypes}.
 */
@ApiStatus.Experimental
public final class MinestomStubParameterType<A extends MinestomCommandActor> implements ParameterType<A, Object> {

    private static final MinestomStubParameterType<MinestomCommandActor> INSTANCE = new MinestomStubParameterType<>();

    @SuppressWarnings("unchecked")
    public static <A extends MinestomCommandActor, T> @NotNull ParameterType<A, T> stubParameterType() {
        return (ParameterType<A, T>) INSTANCE;
    }

    @Override public Object parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<@NotNull A> context) {
        throw new UnsupportedOperationException("This is a stub to tell Lamp that we can parse a certain parameter type. " +
                "Real argument parsing is done in Minestom's 'Argument' classes. This method should never be called, and this " +
                "ParameterType should only be used for types that have corresponding Arguments registered in MinestomArgumentTypes.");
    }

}

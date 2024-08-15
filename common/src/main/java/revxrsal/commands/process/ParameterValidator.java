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
package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.parameter.ParameterType;

/**
 * A validator for a specific parameter type. These validators can do extra checks on parameters
 * after they are resolved from {@link ParameterType}s.
 * <p>
 * Validators work on subclasses as well. For example, we can write a validator to validate
 * a custom <code>@Range(min, max)</code> annotation for numbers:
 *
 * <pre>{@code
 * public enum RangeValidator implements ParameterValidator<CommandActor, Number> {
 *     INSTANCE;
 *
 *     @Override public void validate(@NotNull CommandActor actor, Number value, @NotNull ParameterNode<CommandActor, Number> parameter, @NotNull Lamp<CommandActor> lamp) {
 *         Range range = parameter.getAnnotation(Range.class);
 *         if (range == null) return;
 *         double d = value.doubleValue();
 *         if (d < range.min())
 *             throw new CommandErrorException(actor, "Specified value (" + d + ") is less than minimum " + range.min());
 *         if (d > range.max())
 *             throw new CommandErrorException(actor, "Specified value (" + d + ") is greater than maximum " + range.max());
 *     }
 * }
 * }</pre>
 * <p>
 * These can be registered through {@link Lamp.Builder#parameterValidator(Class, ParameterValidator)}
 *
 * @param <T> The parameter handler
 */
@FunctionalInterface
public interface ParameterValidator<A extends CommandActor, T> {

    /**
     * Validates the specified value that was passed to a parameter.
     * <p>
     * Ideally, a validator will want to throw an exception when the parameter is
     * not valid, and then further handled with {@link CommandExceptionHandler}.
     *
     * @param value     The parameter value. May or may not be null, depending on the resolver.
     * @param parameter The parameter that will take this value
     * @param actor     The command actor
     */
    void validate(@NotNull A actor, T value, @NotNull ParameterNode<A, T> parameter, @NotNull Lamp<A> lamp);

}

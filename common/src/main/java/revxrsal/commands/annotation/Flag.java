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
package revxrsal.commands.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark a parameter as a flag.
 * <p>
 * Flags are similar to normal parameters, however they do not need to
 * come in a specific order, and are explicitly named with a special prefix when the
 * command is invoked.
 * <p>
 * For example, <code>/test (parameters) --name "hello there"</code>, in which <em>name</em> would
 * be a flag parameter.
 * <p>
 * Flags can have a long form and a short form. The long form is prefixed by {@code --},
 * while the short one is prefixed by {@code -}.
 * <p>
 * Flags are compatible with {@link Default} and {@link Optional}, as in, they can be
 * marked as optional or can have a default value when not specified.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Flag {

    /**
     * Returns the long form of the flag name. If left empty,
     * it will use the parameter name.
     *
     * @return The flag's long form
     */
    @NotNull String value() default "";

    /**
     * Returns the short form of the flag name
     *
     * @return The flag's long form
     */
    char shorthand() default '\0';

}

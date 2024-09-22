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
package revxrsal.commands.minestom.argument;

import net.minestom.server.command.builder.arguments.ArgumentType;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.command.CommandActor;

import static revxrsal.commands.util.Classes.wrap;

/**
 * Contains default {@link ArgumentTypeFactory ArgumentTypeFactories}.
 */
final class DefaultTypeFactories {

    public static final ArgumentTypeFactory<CommandActor> STRING = parameter -> {
        if (parameter.type() == String.class)
            return parameter.isGreedy() ? ArgumentType.StringArray(parameter.name())
                    .map(v -> String.join(" ", v))
                    : ArgumentType.String(parameter.name());
        return null;
    };

    public static final ArgumentTypeFactory<CommandActor> INTEGER = parameter -> {
        if (wrap(parameter.type()) == Integer.class) {
            Range range = parameter.annotations().get(Range.class);
            if (range == null)
                return ArgumentType.Integer(parameter.name());
            return ArgumentType.Integer(parameter.name())
                    .min((int) range.min())
                    .max((int) range.max());
        }
        return null;
    };

    public static final ArgumentTypeFactory<CommandActor> SHORT = parameter -> {
        if (wrap(parameter.type()) == Short.class) {
            Range range = parameter.annotations().get(Range.class);
            if (range == null)
                return ArgumentType.Integer(parameter.name())
                        .min((int) Short.MIN_VALUE)
                        .max((int) Short.MAX_VALUE);
            return ArgumentType.Integer(parameter.name())
                    .min((int) Math.max(range.min(), Short.MIN_VALUE))
                    .max((int) Math.min(range.max(), Short.MAX_VALUE));
        }
        return null;
    };

    public static final ArgumentTypeFactory<CommandActor> BYTE = parameter -> {
        if (wrap(parameter.type()) == Byte.class) {
            Range range = parameter.annotations().get(Range.class);
            if (range == null)
                return ArgumentType.Integer(parameter.name())
                        .min((int) Byte.MIN_VALUE)
                        .max((int) Byte.MAX_VALUE);
            return ArgumentType.Integer(parameter.name())
                    .min((int) Math.max(range.min(), Byte.MIN_VALUE))
                    .max((int) Math.min(range.max(), Byte.MAX_VALUE));
        }
        return null;
    };

    public static final ArgumentTypeFactory<CommandActor> LONG = parameter -> {
        if (wrap(parameter.type()) == Long.class) {
            Range range = parameter.annotations().get(Range.class);
            if (range == null)
                return ArgumentType.Long(parameter.name());
            return ArgumentType.Long(parameter.name())
                    .min((long) range.min())
                    .max((long) range.max());
        }
        return null;
    };

    public static final ArgumentTypeFactory<CommandActor> DOUBLE = parameter -> {
        if (wrap(parameter.type()) == Double.class) {
            Range range = parameter.annotations().get(Range.class);
            if (range == null)
                return ArgumentType.Double(parameter.name());
            return ArgumentType.Double(parameter.name())
                    .min(range.min())
                    .max(range.max());
        }
        return null;
    };

    public static final ArgumentTypeFactory<CommandActor> FLOAT = parameter -> {
        if (wrap(parameter.type()) == Float.class) {
            Range range = parameter.annotations().get(Range.class);
            if (range == null)
                return ArgumentType.Float(parameter.name());
            return ArgumentType.Float(parameter.name()).min((float) range.min()).max((float) range.max());
        }
        return null;
    };

    public static final ArgumentTypeFactory<CommandActor> BOOLEAN = ArgumentTypeFactory.forType(boolean.class, p -> ArgumentType.Boolean(p.name()));
    public static final ArgumentTypeFactory<CommandActor> CHAR = ArgumentTypeFactory.forType(char.class, p -> ArgumentType.Word(p.name()));

    private DefaultTypeFactories() {}

}

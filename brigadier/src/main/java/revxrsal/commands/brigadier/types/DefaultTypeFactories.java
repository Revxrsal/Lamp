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
package revxrsal.commands.brigadier.types;

import com.mojang.brigadier.arguments.*;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.command.CommandActor;

import static revxrsal.commands.util.Classes.wrap;

/**
 * Contains default {@link ArgumentTypeFactory ArgumentTypeFactories}.
 */
final class DefaultTypeFactories {

    public static final ArgumentTypeFactory<CommandActor> STRING = parameter -> {
        if (parameter.type() == String.class)
            return parameter.isGreedy() ? StringArgumentType.greedyString() : StringArgumentType.string();
        return null;
    };
    public static final ArgumentTypeFactory<CommandActor> INTEGER = parameter -> {
        if (wrap(parameter.type()) == Integer.class) {
            Range range = parameter.annotations().get(Range.class);
            if (range == null)
                return IntegerArgumentType.integer();
            return IntegerArgumentType.integer((int) range.min(), (int) range.max());
        }
        return null;
    };
    public static final ArgumentTypeFactory<CommandActor> SHORT = parameter -> {
        if (wrap(parameter.type()) == Short.class) {
            Range range = parameter.annotations().get(Range.class);
            if (range == null)
                return IntegerArgumentType.integer(Short.MIN_VALUE, Short.MAX_VALUE);
            return IntegerArgumentType.integer((short) range.min(), (short) range.max());
        }
        return null;
    };
    public static final ArgumentTypeFactory<CommandActor> BYTE = parameter -> {
        if (wrap(parameter.type()) == Byte.class) {
            Range range = parameter.annotations().get(Range.class);
            if (range == null)
                return IntegerArgumentType.integer(Byte.MIN_VALUE, Byte.MAX_VALUE);
            return IntegerArgumentType.integer((byte) range.min(), (byte) range.max());
        }
        return null;
    };
    public static final ArgumentTypeFactory<CommandActor> LONG = parameter -> {
        if (wrap(parameter.type()) == Long.class) {
            Range range = parameter.annotations().get(Range.class);
            if (range == null)
                return LongArgumentType.longArg();
            return LongArgumentType.longArg((long) range.min(), (long) range.max());
        }
        return null;
    };
    public static final ArgumentTypeFactory<CommandActor> DOUBLE = parameter -> {
        if (wrap(parameter.type()) == Double.class) {
            Range range = parameter.annotations().get(Range.class);
            if (range == null)
                return DoubleArgumentType.doubleArg();
            return DoubleArgumentType.doubleArg(range.min(), range.max());
        }
        return null;
    };
    public static final ArgumentTypeFactory<CommandActor> FLOAT = parameter -> {
        if (wrap(parameter.type()) == Double.class) {
            Range range = parameter.annotations().get(Range.class);
            if (range == null)
                return FloatArgumentType.floatArg();
            return FloatArgumentType.floatArg((float) range.min(), (float) range.max());
        }
        return null;
    };
    public static final ArgumentTypeFactory<CommandActor> BOOLEAN = ArgumentTypeFactory.forType(boolean.class, BoolArgumentType.bool());
    public static final ArgumentTypeFactory<CommandActor> CHAR = ArgumentTypeFactory.forType(char.class, StringArgumentType.string());

    private DefaultTypeFactories() {}

}

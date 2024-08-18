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

import com.mojang.brigadier.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.brigadier.annotations.BType;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ParameterNode;

import java.lang.reflect.Constructor;

/**
 * An {@link ArgumentTypeFactory} for dealing with {@link BType @BType} annotations
 */
enum BTypeFactory implements ArgumentTypeFactory<CommandActor> {

    INSTANCE;

    @Override public @Nullable ArgumentType<?> getArgumentType(@NotNull ParameterNode<CommandActor, ?> parameter) {
        BType b = parameter.annotations().get(BType.class);
        if (b == null)
            return null;
        Object v = construct(b.value());
        if (v instanceof ArgumentTypeFactory<?> factory)
            //noinspection rawtypes
            return factory.getArgumentType(((ParameterNode) parameter));
        if (v instanceof ArgumentType<?> type)
            return type;
        throw new IllegalArgumentException("Don't know how to create an ArgumentType from @BType(" + v + ")");
    }

    private @NotNull Object construct(Class<?> value) {
        try {
            Constructor<?> constructor = value.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Throwable t) {
            throw new IllegalArgumentException("Failed to construct the parameter type inside @BType (" + value + ")", t);
        }
    }
}

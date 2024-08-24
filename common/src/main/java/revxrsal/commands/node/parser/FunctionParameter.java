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
package revxrsal.commands.node.parser;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Length;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Sized;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

record FunctionParameter(
        @NotNull Parameter parameter,
        @NotNull String name,
        @NotNull AnnotationList annotations,
        int methodIndex
) implements CommandParameter {

    @Override
    public boolean isLastInMethod() {
        return method().getParameterCount() == methodIndex + 1;
    }

    @Override
    public @NotNull Method method() {
        return ((Method) parameter.getDeclaringExecutable());
    }

    @Override
    public @NotNull Class<?> type() {
        return parameter.getType();
    }

    @Override
    public @NotNull Type fullType() {
        return parameter.getParameterizedType();
    }

    @Override
    public @NotNull List<Type> generics() {
        Type type = parameter.getParameterizedType();
        if (type instanceof ParameterizedType)
            return List.of(((ParameterizedType) type).getActualTypeArguments());
        return List.of();
    }

    @Override
    public boolean isOptional() {
        if (type() == java.util.Optional.class)
            return true;
        if (annotations.contains(Optional.class) || annotations.contains(Default.class))
            return true;
        Sized sized = annotations.get(Sized.class);
        if (sized != null)
            return sized.min() == 0;
        Length length = annotations.get(Length.class);
        if (length != null)
            return length.min() == 0;
        return false;
    }
}

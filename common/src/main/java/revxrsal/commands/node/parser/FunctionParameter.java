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
import revxrsal.commands.annotation.*;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class FunctionParameter implements CommandParameter {
    private final @NotNull Parameter parameter;
    private final @NotNull String name;
    private final @NotNull AnnotationList annotations;
    private final int methodIndex;

    FunctionParameter(
            @NotNull Parameter parameter,
            @NotNull String name,
            @NotNull AnnotationList annotations,
            int methodIndex
    ) {
        this.parameter = parameter;
        this.name = name;
        this.annotations = annotations;
        this.methodIndex = methodIndex;
    }

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
            return Arrays.asList(((ParameterizedType) type).getActualTypeArguments());
        return Collections.emptyList();
    }

    @Override
    public boolean isOptional() {
        if (type() == java.util.Optional.class)
            return true;
        if (annotations.contains(Optional.class) || annotations.contains(Default.class) || annotations.contains(Switch.class))
            return true;
        Sized sized = annotations.get(Sized.class);
        if (sized != null)
            return sized.min() == 0;
        Length length = annotations.get(Length.class);
        if (length != null)
            return length.min() == 0;
        return false;
    }

    @Override public @NotNull Parameter parameter() {return parameter;}

    @Override public @NotNull String name() {return name;}

    @Override public @NotNull AnnotationList annotations() {return annotations;}

    @Override public int methodIndex() {return methodIndex;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        FunctionParameter that = (FunctionParameter) obj;
        return Objects.equals(this.parameter, that.parameter) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.annotations, that.annotations) &&
                this.methodIndex == that.methodIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameter, name, annotations, methodIndex);
    }

    @Override
    public String toString() {
        return "FunctionParameter[" +
                "parameter=" + parameter + ", " +
                "name=" + name + ", " +
                "annotations=" + annotations + ", " +
                "methodIndex=" + methodIndex + ']';
    }

}

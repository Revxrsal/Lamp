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
package revxrsal.commands.parameter.builtins;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.parameter.ContextParameter;

import java.lang.reflect.Type;
import java.util.Objects;

import static revxrsal.commands.util.Classes.getRawType;
import static revxrsal.commands.util.Classes.wrap;

@ApiStatus.Internal
public final class ClassContextParameterFactory<A extends CommandActor, T> implements ContextParameter.Factory<A> {
    private final Class<?> type;
    private final ContextParameter<A, T> parameterType;
    private final boolean allowSubclasses;


    public ClassContextParameterFactory(
            Class<?> type,
            ContextParameter<A, T> parameterType,
            boolean allowSubclasses
    ) {
        this.type = wrap(type);
        this.parameterType = parameterType;
        this.allowSubclasses = allowSubclasses;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <L> ContextParameter<A, L> create(@NotNull Type parameterType, @NotNull AnnotationList annotations, @NotNull Lamp<A> lamp) {
        Class<?> pType = wrap(getRawType(parameterType));
        if (allowSubclasses && type.isAssignableFrom(pType)) {
            return (ContextParameter<A, L>) this.parameterType;
        }
        if (type == pType)
            return (ContextParameter<A, L>) this.parameterType;
        return null;
    }

    @Override
    public String toString() {
        return "ClassContextParameterFactory[" +
                "type=" + type + ", " +
                "parameterType=" + parameterType + ", " +
                "allowSubclasses=" + allowSubclasses + ']';
    }

    public Class<?> type() {return type;}

    public ContextParameter<A, T> parameterType() {return parameterType;}

    public boolean allowSubclasses() {return allowSubclasses;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ClassContextParameterFactory that = (ClassContextParameterFactory) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.parameterType, that.parameterType) &&
                this.allowSubclasses == that.allowSubclasses;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, parameterType, allowSubclasses);
    }


}

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

import static revxrsal.commands.util.Classes.getRawType;
import static revxrsal.commands.util.Classes.wrap;

@ApiStatus.Internal
public record ClassContextParameterFactory<A extends CommandActor, T>(
        Class<?> type,
        ContextParameter<A, T> parameterType,
        boolean allowSubclasses
) implements ContextParameter.Factory<A> {

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

}

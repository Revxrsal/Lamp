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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;

import java.lang.reflect.Type;
import java.util.Optional;

import static revxrsal.commands.util.Classes.getFirstGeneric;
import static revxrsal.commands.util.Classes.getRawType;

/**
 * Handles parameters of type {@link Optional} and is effectively the same as using
 * {@link revxrsal.commands.annotation.Optional @Optional} on such annotations
 */
public enum OptionalParameterTypeFactory implements ParameterType.Factory<CommandActor> {
    INSTANCE;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public @Nullable <T> ParameterType<CommandActor, T> create(
            @NotNull Type parameterType,
            @NotNull AnnotationList annotations,
            @NotNull Lamp<CommandActor> lamp
    ) {
        Class<?> rawType = getRawType(parameterType);
        if (rawType != Optional.class)
            return null;
        Type delegateType = getFirstGeneric(parameterType, Object.class);
        ParameterType<?, ?> delegate = lamp.resolver(delegateType, annotations)
                .requireParameterType();
        return (input, context) -> (T) Optional.of(delegate.parse(input, ((ExecutionContext) context)));
    }
}

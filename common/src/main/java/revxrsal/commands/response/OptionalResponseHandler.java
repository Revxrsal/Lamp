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
package revxrsal.commands.response;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;

import java.lang.reflect.Type;
import java.util.Optional;

import static revxrsal.commands.util.Classes.getFirstGeneric;
import static revxrsal.commands.util.Classes.getRawType;

/**
 * A {@link ResponseHandler.Factory} that creates {@link ResponseHandler}s for
 * {@link Optional {@code Optional<T>}} where T is any type that has
 * a registered response handler.
 */
public enum OptionalResponseHandler implements ResponseHandler.Factory<CommandActor> {

    INSTANCE;

    @Override
    public @Nullable <T> ResponseHandler<CommandActor, T> create(@NotNull Type type, @NotNull AnnotationList annotations, @NotNull Lamp<CommandActor> lamp) {
        Class<?> rawType = getRawType(type);
        if (rawType != Optional.class)
            return null;
        Type suppliedType = getFirstGeneric(type, Object.class);
        ResponseHandler<CommandActor, Object> delegate = lamp.responseHandler(suppliedType, AnnotationList.empty());
        return (response, context) -> {
            //noinspection unchecked
            Optional<Object> optional = (Optional<Object>) response;
            optional.ifPresent(value -> delegate.handleResponse(value, context));
        };
    }
}

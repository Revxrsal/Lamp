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
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Delimiter;
import revxrsal.commands.annotation.Sized;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.InputParseException;
import revxrsal.commands.exception.InvalidListSizeException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;
import revxrsal.commands.util.Classes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public abstract class CollectionParameterTypeFactory implements ParameterType.Factory<CommandActor> {

    protected abstract boolean matchType(@NotNull Type type, @NotNull Class<?> rawType);

    protected abstract Type getElementType(@NotNull Type type);

    protected abstract Object convert(List<Object> items, Type componentType);

    @Override
    public @Nullable <T> ParameterType<CommandActor, T> create(
            @NotNull Type parameterType,
            @NotNull AnnotationList annotations,
            @NotNull Lamp<CommandActor> lamp
    ) {
        Class<?> rawType = Classes.getRawType(parameterType);
        if (!matchType(parameterType, rawType))
            return null;
        Type elementType = getElementType(parameterType);
        if (elementType == null) return null;
        @NotNull ParameterType<CommandActor, Object> componentType = lamp
                .resolver(elementType)
                .requireParameterType(elementType);
        Sized sized = annotations.get(Sized.class);
        int min = 0, max = Integer.MAX_VALUE;
        if (sized != null) {
            min = sized.min();
            max = sized.max();
            //noinspection ConstantValue
            if (min < 0 || max < 0 || max < min)
                throw new IllegalArgumentException("Illegal range input in @Sized");
        }
        char delimiter = annotations.mapOr(Delimiter.class, Delimiter::value, ' ');
        //noinspection unchecked
        return (ParameterType<CommandActor, T>) new CollectionParameterType(delimiter, min, max, componentType, elementType);
    }

    private final class CollectionParameterType implements ParameterType<CommandActor, Object> {
        private final char delimiter;
        private final int minSize, maxSize;
        private final ParameterType<CommandActor, Object> componentType;
        private final PrioritySpec priority;
        private final Type elementType;

        @SuppressWarnings({"rawtypes", "unchecked"})
        public CollectionParameterType(char delimiter, int minSize, int maxSize, ParameterType<CommandActor, Object> componentType, Type elementType) {
            this.delimiter = delimiter;
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.componentType = componentType;
            this.elementType = elementType;

            priority = componentType.parsePriority().toBuilder()
                    .lowerThan(((Class) componentType.getClass()))
                    .build();
        }

        private List<Object> parseList(@NotNull MutableStringStream input, @NotNull ExecutionContext<CommandActor> context) {
            List<Object> elements = new ArrayList<>();
            while (input.hasRemaining()) {
                Object el = componentType.parse(input, context);
                elements.add(el);
                if (input.hasRemaining()) {
                    if (input.peek() == delimiter)
                        input.moveForward();
                    else
                        throw new InputParseException(InputParseException.Cause.EXPECTED_WHITESPACE);
                }
            }
            if (elements.size() > maxSize || elements.size() < minSize)
                throw new InvalidListSizeException(minSize, maxSize, elements.size(), elements);

            return elements;
        }

        @Override
        public Object parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<CommandActor> context) {
            List<Object> objects = parseList(input, context);
            return convert(objects, elementType);
        }

        @Override
        public @NotNull List<String> defaultSuggestions(@NotNull StringStream input, @NotNull CommandActor actor, @NotNull ExecutionContext<CommandActor> context) {
            return ParameterType.super.defaultSuggestions(input, actor, context);
        }

        @Override
        public @NotNull PrioritySpec parsePriority() {
            return priority;
        }

    }

}

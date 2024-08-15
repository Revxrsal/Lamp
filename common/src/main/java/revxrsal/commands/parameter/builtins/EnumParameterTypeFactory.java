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
import revxrsal.commands.exception.EnumNotFoundException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static revxrsal.commands.util.Classes.getRawType;

@ApiStatus.Internal
public enum EnumParameterTypeFactory implements ParameterType.Factory<CommandActor> {
    INSTANCE;

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> ParameterType<CommandActor, T> create(@NotNull Type parameterType, @NotNull AnnotationList annotations, @NotNull Lamp<CommandActor> lamp) {
        Class<?> rawType = getRawType(parameterType);
        if (!rawType.isEnum())
            return null;
        Enum<?>[] enumConstants = (Enum<?>[]) rawType.getEnumConstants();
        Map<String, Enum<?>> byKeys = new HashMap<>();
        List<String> suggestions = new ArrayList<>();
        for (Enum<?> enumConstant : enumConstants) {
            String name = enumConstant.name().toLowerCase();
            byKeys.put(name, enumConstant);
            suggestions.add(name);
        }
        return new EnumParameterType(byKeys, suggestions);
    }

    private record EnumParameterType<E extends Enum<E>>(
            Map<String, E> byKeys,
            List<String> suggestions
    ) implements ParameterType<CommandActor, E> {

        @Override
        public E parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<CommandActor> context) {
            String key = input.readUnquotedString();
            E value = byKeys.get(key.toLowerCase());
            if (value != null)
                return value;
            throw new EnumNotFoundException(key);
        }

        @Override
        public @NotNull List<String> defaultSuggestions(@NotNull StringStream input, @NotNull CommandActor actor, @NotNull ExecutionContext<CommandActor> context) {
            return suggestions;
        }

        @Override
        public @NotNull PrioritySpec parsePriority() {
            return PrioritySpec.highest();
        }
    }
}

/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
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
package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.process.ValueResolverFactory;
import revxrsal.commands.annotation.CaseSensitive;
import revxrsal.commands.exception.EnumNotFoundException;

import java.util.HashMap;
import java.util.Map;

enum EnumResolverFactory implements ValueResolverFactory {

    INSTANCE;

    @Override public @Nullable ValueResolver<?> create(@NotNull CommandParameter parameter) {
        Class<?> type = parameter.getType();
        if (!type.isEnum()) return null;
        Class<? extends Enum> enumType = type.asSubclass(Enum.class);
        Map<String, Enum<?>> values = new HashMap<>();
        boolean caseSensitive = parameter.hasAnnotation(CaseSensitive.class);
        for (Enum<?> enumConstant : enumType.getEnumConstants()) {
            if (caseSensitive)
                values.put(enumConstant.name(), enumConstant);
            else
                values.put(enumConstant.name().toLowerCase(), enumConstant);
        }
        return (ValueResolver<Enum<?>>) context -> {
            String value = context.pop();
            Enum<?> v = values.get(caseSensitive ? value : value.toLowerCase());
            if (v == null)
                throw new EnumNotFoundException(parameter, value);
            return v;
        };
    }
}

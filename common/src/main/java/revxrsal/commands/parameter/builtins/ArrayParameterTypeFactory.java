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
import revxrsal.commands.util.Classes;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;

import static revxrsal.commands.util.Classes.getRawType;

public final class ArrayParameterTypeFactory extends CollectionParameterTypeFactory {

    public static final ArrayParameterTypeFactory INSTANCE = new ArrayParameterTypeFactory();

    @Override
    protected boolean matchType(@NotNull Type type, @NotNull Class<?> rawType) {
        Type elementType = Classes.arrayComponentType(type);
        return elementType != null;
    }

    @Override
    protected Type getElementType(@NotNull Type type) {
        return Classes.arrayComponentType(type);
    }

    @Override
    protected Object convert(List<Object> items, Type componentType) {
        Class<?> arrayComponentType = getRawType(componentType);
        Object array = Array.newInstance(arrayComponentType, items.size());
        for (int i = 0; i < items.size(); i++) {
            Array.set(array, i, items.get(i));
        }
        return array;
    }
}

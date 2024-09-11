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
package revxrsal.commands.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;

import static java.lang.reflect.Modifier.isStatic;
import static revxrsal.commands.util.Preconditions.cannotInstantiate;

/**
 * A utility that tries multiple ways of obtaining an instance
 * of a certain type.
 */
public final class InstanceCreator {

    private InstanceCreator() {
        cannotInstantiate(InstanceCreator.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> @NotNull T create(@NotNull Class<? extends T> type) {
        if (type.isAnnotation())
            throw new IllegalArgumentException("Cannot construct annotation types");
        if (type.isRecord())
            throw new IllegalArgumentException("Cannot construct record types");
        if (type.isArray())
            return (T) Array.newInstance(type, 0);
        if (type.isEnum())
            return (T) firstEnum(type.asSubclass(Enum.class));
        if (type.isInterface()) {
            T singleton = fromSingletonField(type);
            if (singleton != null)
                return singleton;
            singleton = fromGetter(type);
            if (singleton == null)
                throw new IllegalArgumentException("Attempted to construct an interface that has no getInstance()-like methods or INSTANCE-like fields");
        }
        T t = fromNoArgConstructor(type);
        if (t == null)
            t = fromSingletonField(type);
        if (t != null)
            return t;
        t = fromGetter(type);
        if (t == null)
            throw new IllegalArgumentException("Attempted to construct a class that has no getInstance() or INSTANCE, singletons, or a no-arg constructor.");
        return t;
    }

    private static <T> @Nullable T fromNoArgConstructor(Class<? extends T> type) {
        try {
            Constructor<? extends T> constructor = type.getDeclaredConstructor();
            constructor.trySetAccessible();
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T fromGetter(Class<? extends T> type) {
        for (Method method : type.getDeclaredMethods()) {
            if (!type.isAssignableFrom(method.getReturnType()))
                continue;
            if (!isStatic(method.getModifiers()))
                continue;
            if (method.getParameterCount() != 0)
                continue;
            method.trySetAccessible();
            try {
                return (T) method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static @NotNull <T extends Enum<? extends T>> T firstEnum(Class<? extends T> type) {
        T[] values = type.getEnumConstants();
        if (values.length == 0)
            throw new IllegalArgumentException("Attempted to construct an enum that has no fields");
        return values[0];
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable T fromSingletonField(Class<? extends T> type) {
        for (Field field : type.getDeclaredFields()) {
            if (!type.isAssignableFrom(field.getType()))
                continue;
            if (!isStatic(field.getModifiers()))
                continue;
            field.trySetAccessible();
            try {
                return (T) field.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}

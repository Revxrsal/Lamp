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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static revxrsal.commands.util.Preconditions.cannotInstantiate;
import static revxrsal.commands.util.Preconditions.notNull;

/**
 * A utility class for dealing with wrapping and unwrapping of primitive
 * types
 */
public final class Classes {

    private Classes() {
        cannotInstantiate(Classes.class);
    }

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER;
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE;

    /**
     * Returns the type of the given object
     *
     * @param o Object to get for
     * @return The object type
     */
    public static Class<?> getType(@NotNull Object o) {
        return o instanceof Class ? (Class<?>) o : o.getClass();
    }

    /**
     * Returns the corresponding wrapper type of {@code type} if it is a primitive type; otherwise
     * returns the type itself.
     *
     * <pre>
     *     wrap(int.class) == Integer.class
     *     wrap(Integer.class) == Integer.class
     *     wrap(String.class) == String.class
     * </pre>
     */
    public static <T> Class<T> wrap(Class<T> type) {
        notNull(type, "type");
        Class<T> wrapped = (Class<T>) PRIMITIVE_TO_WRAPPER.get(type);
        return (wrapped == null) ? type : wrapped;
    }

    /**
     * Returns the array type of the given {@code type}. This
     * will respect generics of the array type.
     *
     * @param type The type to parse from
     * @return The array type, or {@code null} if it does
     * not represent an array.
     */
    public static @Nullable Type arrayComponentType(Type type) {
        if (type instanceof GenericArrayType) {
            return ((GenericArrayType) type).getGenericComponentType();
        } else if (type instanceof Class) {
            return ((Class<?>) type).getComponentType();
        } else {
            return null;
        }
    }

    /**
     * Returns the corresponding primitive type of {@code type} if it is a wrapper type; otherwise
     * returns the type itself.
     *
     * <pre>
     *     unwrap(Integer.class) == int.class
     *     unwrap(int.class) == int.class
     *     unwrap(String.class) == String.class
     * </pre>
     */
    public static <T> Class<T> unwrap(Class<T> type) {
        notNull(type, "type");
        Class<T> unwrapped = (Class<T>) WRAPPER_TO_PRIMITIVE.get(type);
        return (unwrapped == null) ? type : unwrapped;
    }

    /**
     * Returns {@code true} if the specified type is one of the nine
     * primitive-wrapper types, such as {@link Integer}.
     *
     * @param type Type to check
     * @see Class#isPrimitive
     */
    public static boolean isWrapperType(Class<?> type) {
        notNull(type, "type");
        return WRAPPER_TO_PRIMITIVE.containsKey(type);
    }

    /**
     * Returns the {@link Class} object representing the class or interface
     * that declared this type.
     *
     * @return the {@link Class} object representing the class or interface
     * that declared this type
     */
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            // type is a normal class.
            return (Class<?>) type;

        } else if (type instanceof ParameterizedType parameterizedType) {

            // I'm not exactly sure why getRawType() returns Type instead of Class.
            // Neal isn't either but suspects some pathological case related
            // to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) {
                throw new IllegalStateException("Expected a Class, found a " + rawType);
            }
            return (Class<?>) rawType;

        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();

        } else if (type instanceof TypeVariable) {
            // we could use the variable's bounds, but that won't work if there are multiple.
            // having a raw type that's more general than necessary is okay
            return Object.class;

        } else if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);

        } else {
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                    + "GenericArrayType, but <" + type + "> is of type " + className);
        }
    }

    /**
     * Returns the first generic type of the given (possibly parameterized)
     * type {@code genericType}. If the type is not parameterized,
     * this will return {@code fallback}.
     *
     * @param genericType The generic type
     * @param fallback    The fallback to return
     * @return The generic type
     */
    public static Type getFirstGeneric(Type genericType, Type fallback) {
        try {
            return ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            return fallback;
        }
    }

    static {
        Map<Class<?>, Class<?>> primToWrap = new LinkedHashMap<>(16);
        Map<Class<?>, Class<?>> wrapToPrim = new LinkedHashMap<>(16);

        addPrimitive(primToWrap, wrapToPrim, boolean.class, Boolean.class);
        addPrimitive(primToWrap, wrapToPrim, byte.class, Byte.class);
        addPrimitive(primToWrap, wrapToPrim, char.class, Character.class);
        addPrimitive(primToWrap, wrapToPrim, double.class, Double.class);
        addPrimitive(primToWrap, wrapToPrim, float.class, Float.class);
        addPrimitive(primToWrap, wrapToPrim, int.class, Integer.class);
        addPrimitive(primToWrap, wrapToPrim, long.class, Long.class);
        addPrimitive(primToWrap, wrapToPrim, short.class, Short.class);
        addPrimitive(primToWrap, wrapToPrim, void.class, Void.class);

        PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(primToWrap);
        WRAPPER_TO_PRIMITIVE = Collections.unmodifiableMap(wrapToPrim);
    }

    private static void addPrimitive(
            Map<Class<?>, Class<?>> forward,
            Map<Class<?>, Class<?>> backward,
            Class<?> key,
            Class<?> value
    ) {
        forward.put(key, value);
        backward.put(value, key);
    }
}

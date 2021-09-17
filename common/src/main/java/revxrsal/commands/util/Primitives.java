package revxrsal.commands.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static revxrsal.commands.util.Preconditions.notNull;

/**
 * A utility class for dealing with wrapping and unwrapping of primitive
 * types
 */
public final class Primitives {

    private Primitives() {}

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

    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            // type is a normal class.
            return (Class<?>) type;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

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

    static {
        Map<Class<?>, Class<?>> primToWrap = new LinkedHashMap<>(16);
        Map<Class<?>, Class<?>> wrapToPrim = new LinkedHashMap<>(16);

        add(primToWrap, wrapToPrim, boolean.class, Boolean.class);
        add(primToWrap, wrapToPrim, byte.class, Byte.class);
        add(primToWrap, wrapToPrim, char.class, Character.class);
        add(primToWrap, wrapToPrim, double.class, Double.class);
        add(primToWrap, wrapToPrim, float.class, Float.class);
        add(primToWrap, wrapToPrim, int.class, Integer.class);
        add(primToWrap, wrapToPrim, long.class, Long.class);
        add(primToWrap, wrapToPrim, short.class, Short.class);
        add(primToWrap, wrapToPrim, void.class, Void.class);

        PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(primToWrap);
        WRAPPER_TO_PRIMITIVE = Collections.unmodifiableMap(wrapToPrim);
    }

    private static void add(
            Map<Class<?>, Class<?>> forward,
            Map<Class<?>, Class<?>> backward,
            Class<?> key,
            Class<?> value) {
        forward.put(key, value);
        backward.put(value, key);
    }
}

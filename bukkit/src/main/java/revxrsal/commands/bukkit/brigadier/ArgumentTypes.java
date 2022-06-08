/*
 * This file is part of commodore, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
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

package revxrsal.commands.bukkit.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import org.bukkit.NamespacedKey;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;

/**
 * A registry of the {@link ArgumentType}s provided by Minecraft.
 */
public final class ArgumentTypes {
    private ArgumentTypes() {}

    // MinecraftKey constructor
    private static final Constructor<?> MINECRAFT_KEY_CONSTRUCTOR;

    // ArgumentRegistry#getByKey (obfuscated) method
    private static final Method ARGUMENT_REGISTRY_GET_BY_KEY_METHOD;

    private static final Field BY_CLASS_MAP_FIELD;

    static {
        try {
            final Class<?> minecraftKey;
            final Class<?> argumentRegistry;

            if (ReflectionUtil.minecraftVersion() > 16) {
                minecraftKey = ReflectionUtil.mcClass("resources.MinecraftKey");
                argumentRegistry = ReflectionUtil.mcClass("commands.synchronization.ArgumentRegistry");
            } else {
                minecraftKey = ReflectionUtil.nmsClass("MinecraftKey");
                argumentRegistry = ReflectionUtil.nmsClass("ArgumentRegistry");
            }

            MINECRAFT_KEY_CONSTRUCTOR = minecraftKey.getConstructor(String.class, String.class);
            MINECRAFT_KEY_CONSTRUCTOR.setAccessible(true);

            ARGUMENT_REGISTRY_GET_BY_KEY_METHOD = Arrays.stream(argumentRegistry.getDeclaredMethods())
                    .filter(method -> method.getParameterCount() == 1)
                    .filter(method -> minecraftKey.equals(method.getParameterTypes()[0]))
                    .findFirst().orElseThrow(NoSuchMethodException::new);
            ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.setAccessible(true);

            BY_CLASS_MAP_FIELD = Arrays.stream(argumentRegistry.getDeclaredFields())
                    .filter(field -> Modifier.isStatic(field.getModifiers()))
                    .filter(field -> field.getType().equals(Map.class))
                    .filter(field -> {
                        final ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                        final Type param = parameterizedType.getActualTypeArguments()[0];
                        if (!(param instanceof ParameterizedType)) {
                            return false;
                        }
                        return ((ParameterizedType) param).getRawType().equals(Class.class);
                    })
                    .findFirst()
                    .orElseThrow(NoSuchFieldException::new);
            BY_CLASS_MAP_FIELD.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Gets if a argument is registered for the given key.
     *
     * @param key the key
     * @return if an argument is registered
     */
    public static boolean isRegistered(NamespacedKey key) {
        try {
            Object minecraftKey = MINECRAFT_KEY_CONSTRUCTOR.newInstance(key.getNamespace(), key.getKey());
            Object entry = ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.invoke(null, minecraftKey);
            return entry != null;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a registered argument type class by key.
     *
     * @param key the key
     * @return the returned argument type class
     * @throws IllegalArgumentException if no such argument is registered
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends ArgumentType<?>> getClassByKey(NamespacedKey key) throws IllegalArgumentException {
        try {
            Object minecraftKey = MINECRAFT_KEY_CONSTRUCTOR.newInstance(key.getNamespace(), key.getKey());
            Object entry = ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.invoke(null, minecraftKey);
            if (entry == null) {
                throw new IllegalArgumentException(key.toString());
            }

            final Map<Class<?>, Object> map = (Map<Class<?>, Object>) BY_CLASS_MAP_FIELD.get(null);
            for (final Map.Entry<Class<?>, Object> mapEntry : map.entrySet()) {
                if (mapEntry.getValue() == entry) {
                    return (Class<? extends ArgumentType<?>>) mapEntry.getKey();
                }
            }
            throw new IllegalArgumentException(key.toString());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a registered argument type by key.
     *
     * @param key the key
     * @return the returned argument
     * @throws IllegalArgumentException if no such argument is registered
     */
    public static ArgumentType<?> getByKey(NamespacedKey key) throws IllegalArgumentException {
        try {
            final Constructor<? extends ArgumentType<?>> constructor = getClassByKey(key).getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static void ensureSetup() {
        // do nothing - this is only called to trigger the static initializer
    }
}

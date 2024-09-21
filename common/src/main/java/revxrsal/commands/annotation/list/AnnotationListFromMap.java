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
package revxrsal.commands.annotation.list;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.annotation.DistributeOnMethods;
import revxrsal.commands.annotation.dynamic.AnnotationReplacer;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Collections.emptySet;
import static revxrsal.commands.util.Classes.checkRetention;
import static revxrsal.commands.util.Collections.unmodifiableIterator;

final class AnnotationListFromMap implements AnnotationList {

    private final Map<Class<? extends Annotation>, Annotation> annotations;

    public AnnotationListFromMap(Map<Class<? extends Annotation>, Annotation> annotations) {
        this.annotations = annotations;
    }

    public static @NotNull AnnotationList createFrom(@NotNull Collection<Annotation> annotations) {
        if (annotations.isEmpty())
            return AnnotationList.empty();
        return new AnnotationListFromMap(toMap(annotations));
    }

    public static @NotNull AnnotationList createFor(@NotNull AnnotatedElement element) {
        Map<Class<? extends Annotation>, Annotation> annotations = toMap(element.getAnnotations());
        if (annotations.isEmpty())
            return AnnotationList.empty();
        return new AnnotationListFromMap(annotations);
    }

    public static @NotNull Map<Class<? extends Annotation>, Annotation> toMap(@NotNull Iterable<Annotation> annotations) {
        Map<Class<? extends Annotation>, Annotation> map = new HashMap<>();
        for (Annotation annotation : annotations) {
            map.put(annotation.annotationType(), annotation);
        }
        return map;
    }

    public static @NotNull Map<Class<? extends Annotation>, Annotation> toMap(@NotNull Annotation[] annotations) {
        Map<Class<? extends Annotation>, Annotation> map = new HashMap<>();
        for (Annotation annotation : annotations) {
            map.put(annotation.annotationType(), annotation);
        }
        return map;
    }

    private static void distributeAnnotations(
            @NotNull Map<Class<? extends Annotation>, Annotation> annotations,
            @NotNull Method element,
            @NotNull Map<Class<? extends Annotation>, Set<AnnotationReplacer<?>>> replacers
    ) {
        Class<?> top = element.getDeclaringClass();
        while (top != null) {
            var classAnnotations = AnnotationList.create(top)
                    .replaceAnnotations(top, replacers);
            for (Annotation annotation : classAnnotations) {
                if (annotation.annotationType().isAnnotationPresent(DistributeOnMethods.class))
                    annotations.putIfAbsent(annotation.annotationType(), annotation);
            }
            top = top.getDeclaringClass();
        }
    }

    @Override
    public <T extends Annotation> @Nullable T get(@NotNull Class<T> type) {
        checkRetention(type);
        //noinspection unchecked
        return (T) annotations.get(type);
    }

    @Override
    public <R, T extends Annotation> @Nullable R map(@NotNull Class<T> type, Function<T, R> function) {
        T annotation = get(type);
        if (annotation != null)
            return function.apply(annotation);
        return null;
    }

    @Override
    public <R, T extends Annotation> R mapOr(@NotNull Class<T> type, Function<T, R> function, R defaultValue) {
        T annotation = get(type);
        if (annotation != null)
            return function.apply(annotation);
        return defaultValue;
    }

    @Override
    public <R, T extends Annotation> R mapOrGet(@NotNull Class<T> type, @NotNull Function<T, R> function, @NotNull Supplier<R> defaultValue) {
        T annotation = get(type);
        if (annotation != null)
            return function.apply(annotation);
        return defaultValue.get();
    }

    @Override
    public <T extends Annotation> @NotNull T require(@NotNull Class<T> type, @NotNull String errorMessage) {
        T annotation = get(type);
        if (annotation == null)
            throw new IllegalStateException(errorMessage);
        return annotation;
    }

    @Override
    public <T extends Annotation> boolean contains(@NotNull Class<T> type) {
        checkRetention(type);
        return annotations.containsKey(type);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public @NotNull AnnotationList replaceAnnotations(
            @NotNull AnnotatedElement element,
            @NotNull Map<Class<? extends Annotation>, Set<AnnotationReplacer<?>>> replacers
    ) {
        Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>(this.annotations);
        for (Annotation annotation : this.annotations.values()) {
            for (AnnotationReplacer replacer : replacers.getOrDefault(annotation.annotationType(), emptySet())) {
                Collection<Annotation> newAnnotations = replacer.replaceAnnotation(element, annotation);
                if (newAnnotations != null)
                    annotations.putAll(toMap(newAnnotations));
            }
        }
        if (element instanceof Method method) {
            distributeAnnotations(annotations, method, replacers);
        }
        return new AnnotationListFromMap(annotations);
    }

    @Override
    public @NotNull Map<Class<?>, Annotation> toMutableMap() {
        return new HashMap<>(annotations);
    }

    @Override
    public boolean isEmpty() {
        return annotations.isEmpty();
    }

    @NotNull
    @Override
    public @UnmodifiableView Iterator<Annotation> iterator() {
        return unmodifiableIterator(annotations.values().iterator());
    }

    @Override
    public boolean any(@NotNull Predicate<Annotation> predicate) {
        for (Annotation value : annotations.values())
            if (predicate.test(value))
                return true;
        return false;
    }

    @Override
    public @NotNull AnnotationList withAnnotations(boolean overrideExisting, @NotNull Annotation... annotations) {
        var map = new HashMap<>(this.annotations);
        for (@NotNull Annotation annotation : annotations) {
            if (overrideExisting)
                map.put(annotation.annotationType(), annotation);
            else
                map.putIfAbsent(annotation.annotationType(), annotation);
        }
        return new AnnotationListFromMap(map);
    }
}

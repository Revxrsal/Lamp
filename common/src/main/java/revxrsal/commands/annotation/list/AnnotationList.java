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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.annotation.DistributeOnMethods;
import revxrsal.commands.annotation.dynamic.AnnotationReplacer;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents an immutable collection of annotations. This interface
 * provides utility methods for dealing with annotations, while respecting
 * {@link DistributeOnMethods} and {@link AnnotationReplacer}s.
 */
public interface AnnotationList extends Iterable<Annotation> {

    /**
     * Creates a new {@link AnnotationList} for the given {@link AnnotatedElement}. This
     * method will take into account {@link DistributeOnMethods} annotations
     *
     * @param element The annotated element to create from
     * @return The newly created annotation list
     */
    static @NotNull AnnotationList create(@NotNull AnnotatedElement element) {
        return AnnotationListFromMap.createFor(element);
    }

    /**
     * Creates a new {@link AnnotationList} for the given collection of annotations. This
     * method will <em>not</em> take into account {@link DistributeOnMethods} annotations
     *
     * @param annotations The annotations in this list
     * @return The newly created annotation list
     */
    static @NotNull AnnotationList create(@NotNull Collection<Annotation> annotations) {
        return AnnotationListFromMap.createFrom(annotations);
    }

    /**
     * Creates a new {@link AnnotationList} for the given map of annotations. This
     * method will <em>not</em> take into account {@link DistributeOnMethods} annotations
     *
     * @param annotations The annotations in this list
     * @return The newly created annotation list
     */
    static @NotNull AnnotationList create(@NotNull Map<Class<? extends Annotation>, Annotation> annotations) {
        if (annotations.isEmpty())
            return empty();
        return new AnnotationListFromMap(annotations);
    }

    /**
     * Returns an empty {@link AnnotationList}.
     *
     * @return The empty {@link AnnotationList} singleton.
     */
    @Contract(pure = true)
    static @NotNull AnnotationList empty() {
        return EmptyAnnotationList.INSTANCE;
    }

    /**
     * Returns the given annotation, or {@code null} if it's not annotated
     * with the given annotation (or distributed on, indirectly)
     *
     * @param type The annotation type
     * @param <T>  The annotation type automatically cast
     * @return The annotation instance, or {@code null} if not
     * present
     */
    @Contract(pure = true)
    <T extends Annotation> @Nullable T get(@NotNull Class<T> type);

    /**
     * Performs a map function on the annotation if it is present, otherwise
     * returns {@code null} if the annotation is not present.
     *
     * @param type     The annotation type
     * @param function The mapping function.
     * @param <R>      The type we are mapping to
     * @param <T>      The annotation type
     * @return The mapped value, or {@code null} if the annotation is not present.
     */
    @Contract(pure = true)
    <R, T extends Annotation> @Nullable R map(@NotNull Class<T> type, Function<T, @Nullable R> function);

    /**
     * Performs a map function on the annotation if it is present, otherwise
     * returns {@code defaultValue} if the annotation is not present.
     *
     * @param type     The annotation type
     * @param function The mapping function.
     * @param <R>      The type we are mapping to
     * @param <T>      The annotation type
     * @return The mapped value, or {@code defaultValue} if the annotation is not present.
     */
    @Contract(pure = true)
    <R, T extends Annotation> R mapOr(@NotNull Class<T> type, Function<T, R> function, R defaultValue);

    /**
     * Performs a map function on the annotation if it is present, otherwise
     * supplies the value in {@code defaultValue} if the annotation is not present.
     *
     * @param type     The annotation type
     * @param function The mapping function.
     * @param <R>      The type we are mapping to
     * @param <T>      The annotation type
     * @return The mapped value, or {@code defaultValue} if the annotation is not present.
     */
    @Contract(pure = true)
    <R, T extends Annotation> R mapOrGet(@NotNull Class<T> type, @NotNull Function<T, R> function, @NotNull Supplier<R> defaultValue);

    /**
     * Gets the given annotation, otherwise throws an {@link IllegalStateException}
     * if it is not present.
     *
     * @param type         Annotation type to check for
     * @param errorMessage The error message to include in the error
     * @param <T>          The annotation type
     * @return The annotation
     */
    @Contract(pure = true)
    <T extends Annotation> @NotNull T require(@NotNull Class<T> type, @NotNull String errorMessage);

    /**
     * Tests whether this list contains the given annotation or not
     *
     * @param type The annotation type to check for
     * @param <T>  The annotation type
     * @return {@code true} if the annotation is present, {@code false} if otherwise.
     */
    @Contract(pure = true)
    <T extends Annotation> boolean contains(@NotNull Class<T> type);

    /**
     * Tests whether this list is empty or not
     *
     * @return if this annotation list is empty
     */
    boolean isEmpty();

    /**
     * Returns a mutable map that contains the annotations contained
     * in this annotation list
     *
     * @return The newly created map
     */
    @NotNull
    @Contract(value = "-> new", pure = true)
    Map<Class<?>, Annotation> toMutableMap();

    /**
     * Replaces the annotations in this type with any suitable {@link AnnotationReplacer}s in
     * the supplied {@code replacers} map, and returns a (possibly new) {@link AnnotationList}
     * with the replaced annotations.
     * <p>
     * Note that this does <em>not</em> modify this {@link AnnotationList}.
     * <p>
     * This also will <em>not</em> deeply replace annotations. If a replacer
     * produces a replaceable annotation, this annotation will not be replaced.
     * <p>
     * In cases of {@link AnnotationList#empty()}, this will return the same annotation
     * list.
     *
     * @param element   The annotated element. This provides additional context and allows
     *                  {@link DistributeOnMethods} annotations
     * @param replacers The annotation replacers to use
     * @return A (possibly new) {@link AnnotationList} with the newly replaced
     * annotations.
     */
    @NotNull
    @Contract(pure = true)
    AnnotationList replaceAnnotations(@NotNull AnnotatedElement element, @NotNull Map<Class<? extends Annotation>, Set<AnnotationReplacer<?>>> replacers);

    /**
     * Returns an unmodifiable {@link Iterator} for this annotation list
     *
     * @return The iterator
     */
    @NotNull
    @Unmodifiable
    @Contract(pure = true)
    Iterator<Annotation> iterator();

    /**
     * Creates a new {@link AnnotationList} that contains these annotations as well
     * as the given {@code annotations}.
     *
     * @param overrideExisting Whether should existing annotations be overridden if
     *                         a similar annotation is supplied
     * @param annotations      The annotations to include in the new copy
     * @return The new annotation list.
     */
    @NotNull
    @Contract(pure = true, value = "_, _ -> new")
    AnnotationList withAnnotations(boolean overrideExisting, @NotNull Annotation... annotations);

    /**
     * Creates a new {@link AnnotationList} that contains these annotations as well
     * as the given {@code annotations}.
     *
     * @param annotations The annotations to include in the new copy
     * @return The new annotation list.
     */
    @NotNull
    @Contract(pure = true, value = "_ -> new")
    default AnnotationList withAnnotations(@NotNull Annotation... annotations) {
        return withAnnotations(true, annotations);
    }

    /**
     * Tests whether any of the annotations in this list match
     * the given predicate
     *
     * @param predicate Predicate to check with
     * @return true if any annotation matches the predicate, false if otherwise.
     */
    @Contract(pure = true)
    boolean any(@NotNull Predicate<Annotation> predicate);
}

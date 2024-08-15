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
import revxrsal.commands.annotation.dynamic.AnnotationReplacer;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class EmptyAnnotationList implements AnnotationList {

    public static final EmptyAnnotationList INSTANCE = new EmptyAnnotationList();

    @Override
    public <T extends Annotation> @Nullable T get(@NotNull Class<T> type) {
        return null;
    }

    @Override
    public <R, T extends Annotation> @Nullable R map(@NotNull Class<T> type, Function<T, R> function) {
        return null;
    }

    @Override
    public <R, T extends Annotation> R mapOr(@NotNull Class<T> type, Function<T, R> function, R defaultValue) {
        return defaultValue;
    }

    @Override
    public <R, T extends Annotation> R mapOrGet(@NotNull Class<T> type, @NotNull Function<T, R> function, @NotNull Supplier<R> defaultValue) {
        return defaultValue.get();
    }

    @Override
    public <T extends Annotation> @NotNull T require(@NotNull Class<T> type, @NotNull String errorMessage) {
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public <T extends Annotation> boolean contains(@NotNull Class<T> type) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public @NotNull AnnotationList replaceAnnotations(@NotNull AnnotatedElement element, @NotNull Map<Class<? extends Annotation>, Set<AnnotationReplacer<?>>> replacers) {
        return this;
    }

    @NotNull
    @Override
    public @UnmodifiableView Iterator<Annotation> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public @NotNull Map<Class<?>, Annotation> toMutableMap() {
        return new HashMap<>();
    }

    @Override
    public @NotNull AnnotationList withAnnotations(boolean overrideExisting, @NotNull Annotation... annotations) {
        var map = AnnotationListFromMap.toMap(annotations);
        return new AnnotationListFromMap(map);
    }

    @Override
    public boolean any(@NotNull Predicate<Annotation> predicate) {
        return false;
    }
}

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
package revxrsal.commands.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.reflect.MethodCaller;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static revxrsal.commands.util.Collections.copyList;

/**
 * A utility for stripping stacktrace from local paths to classes. This helps
 * keep errors clean as well as removing unnecessary trace paths which do not
 * help.
 */
public final class StackTraceSanitizer {

    /**
     * The default stack trace sanitizer
     */
    private static final StackTraceSanitizer DEFAULT_SANITIZER = StackTraceSanitizer.builder()
            .ignoreClasses(Lamp.class)
            .ignoreClasses(MethodHandles.class, MethodHandle.class)
            .ignorePackage(MethodCaller.class.getPackage())
            .build();

    /**
     * A stack trace sanitizer that does not do anything.
     */
    private static final StackTraceSanitizer EMPTY = new StackTraceSanitizer(emptyList());

    /**
     * A list of all predicates that remove matching elements
     */
    private final @Unmodifiable List<Predicate<StackTraceElement>> filters;

    private StackTraceSanitizer(@Unmodifiable List<Predicate<StackTraceElement>> filters) {
        this.filters = filters;
    }

    /**
     * Returns the default sanitizer
     *
     * @return The default stack trace sanitizer
     */
    @Contract(pure = true)
    public static @NotNull StackTraceSanitizer defaultSanitizer() {
        return DEFAULT_SANITIZER;
    }

    /**
     * Returns the empty sanitizer
     *
     * @return A sanitizer that does not modify the trace
     */
    @Contract(pure = true)
    public static @NotNull StackTraceSanitizer none() {
        return EMPTY;
    }

    /**
     * Returns a new {@link StackTraceSanitizer.Builder}
     *
     * @return A new builder
     */
    @Contract("-> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Strips all the stack trace elements that meet the criteria of any
     * filter.
     *
     * @param throwable Throwable to strip
     */
    public void sanitize(@NotNull Throwable throwable) {
        if (filters.isEmpty()) return;
        if (throwable.getCause() != null)
            sanitize(throwable.getCause());
        List<StackTraceElement> trace = new ArrayList<>();
        java.util.Collections.addAll(trace, throwable.getStackTrace());
        int stripIndex = trace.size();
        for (int i = 0; i < trace.size(); i++) {
            StackTraceElement stackTraceElement = trace.get(i);
            if (filters.stream().anyMatch(f -> f.test(stackTraceElement))) {
                stripIndex = i;
                break;
            }
        }
        trace.subList(stripIndex, trace.size()).clear();
        throwable.setStackTrace(trace.toArray(new StackTraceElement[0]));
    }

    public static class Builder {

        private final List<Predicate<StackTraceElement>> filters = new ArrayList<>();

        /**
         * Strips all the given classes from the trace
         *
         * @param classes Classes to strip
         * @return This builder
         */
        public Builder ignoreClasses(@NotNull Class<?>... classes) {
            for (Class<?> clazz : classes)
                filters.add(c -> c.getClassName().equals(clazz.getName()));
            return this;
        }

        /**
         * Strips all classes that belong to the given package name
         *
         * @param packageName Package name to strip
         * @return This builder
         */
        public Builder ignorePackage(@NotNull String packageName) {
            filters.add(c -> c.getClassName().startsWith(packageName));
            return this;
        }

        /**
         * Strips all classes that belong to the given package
         *
         * @param pkg Package to strip its classes
         * @return This builder
         */

        public Builder ignorePackage(@NotNull Package pkg) {
            filters.add(c -> c.getClassName().startsWith(pkg.getName()));
            return this;
        }

        /**
         * Strips all traces that point to the given method name
         *
         * @param methodName The method name to strip
         * @return This builder
         */
        public Builder ignoreMethod(@NotNull String methodName) {
            filters.add(c -> c.getMethodName().equals(methodName));
            return this;
        }

        /**
         * Strips all native methods.
         *
         * @return This builder
         */
        public Builder ignoreNativeMethods() {
            filters.add(StackTraceElement::isNativeMethod);
            return this;
        }

        /**
         * Constructs the {@link StackTraceSanitizer} from the configuration
         *
         * @return The newly created sanitizer
         */
        public StackTraceSanitizer build() {
            return new StackTraceSanitizer(copyList(filters));
        }
    }

}

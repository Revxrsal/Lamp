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
package revxrsal.commands.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import static revxrsal.commands.util.Classes.checkRetention;

/**
 * Represents a permission that is required in order to execute a
 * command.
 * <p>
 * This implementation may vary depending on the target platform
 */
@FunctionalInterface
public interface CommandPermission<A extends CommandActor> {

    /**
     * Returns a {@link CommandPermission} that always returns true for
     * all actors.
     *
     * @param <A> The actor type
     * @return The permission
     */
    static <A extends CommandActor> CommandPermission<A> alwaysTrue() {
        return actor -> true;
    }

    /**
     * Returns whether the sender has permission to use this command
     * or not.
     *
     * @param actor Actor to test against
     * @return {@code true} if they can use it, false if otherwise.
     */
    boolean isExecutableBy(@NotNull A actor);

    /**
     * Represents a convenient way to register custom {@link CommandPermission}
     * implementations. This reader can have access to a command's annotations.
     */
    @FunctionalInterface
    interface Factory<A extends CommandActor> {

        /**
         * Creates a {@link Factory} that creates a {@link CommandPermission} for every
         * function or parameter that contains a certain
         *
         * @param annotationType    The annotation type
         * @param permissionCreator The function that creates a {@link CommandPermission}
         *                          for the given annotation.
         * @param <A>               The actor type
         * @param <T>               The annotation type
         * @return The newly created {@link Factory}
         */
        static <A extends CommandActor, T extends Annotation> Factory<A> forAnnotation(
                @NotNull Class<T> annotationType,
                @NotNull Function<T, @Nullable CommandPermission<A>> permissionCreator
        ) {
            checkRetention(annotationType);
            return (annotations, lamp) -> {
                T annotation = annotations.get(annotationType);
                if (annotation != null)
                    return permissionCreator.apply(annotation);
                return null;
            };
        }

        /**
         * Creates a new {@link CommandPermission} for the given list of annotations. This
         * may be a parameter or a function (method).
         * <p>
         * If this factory is not responsible for the given input, it may return
         * {@code null}.
         *
         * @param annotations The annotation list
         * @param lamp        The {@link Lamp} instance
         * @return The permission
         */
        @Nullable CommandPermission<A> create(@NotNull AnnotationList annotations, @NotNull Lamp<A> lamp);

    }

}

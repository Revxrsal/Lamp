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
package revxrsal.commands.response;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;

import java.lang.reflect.Type;

/**
 * A handler for post-handling command responses (results returned from the
 * command methods)
 *
 * @param <T> The response type
 */
public interface ResponseHandler<A extends CommandActor, T> {

    /**
     * Returns a basic {@link ResponseHandler} that does nothing.
     *
     * @param <A> The actor type
     * @param <T> The response type
     * @return The response handler
     */
    static <A extends CommandActor, T> @NotNull ResponseHandler<A, T> noOp() {
        return (response, context) -> {
        };
    }

    /**
     * Handles the response returned from the method
     *
     * @param response The response returned from the method. May or may not be null.
     * @param context  The command execution context
     */
    void handleResponse(T response, ExecutionContext<A> context);

    /**
     * A factory that allows to create {@link ResponseHandler}s dynamically. This
     * factory can access the response {@link Type} and function annotations, as
     * well as other registered response handlers.
     * <p>
     * This may be used to create more complex response handlers by
     * composing them, such as {@code Optional<T>}, {@code Supplier<T>}, etc.
     *
     * @param <A> Actor type
     */
    interface Factory<A extends CommandActor> {

        /**
         * Creates a new {@link ParameterType.Factory} that returns a {@link ParameterType} for all
         * parameters that <em>exactly</em> have a certain class.
         *
         * @param type            Parameter type to check against
         * @param responseHandler The parameter type to supply
         * @param <A>             The actor type
         * @param <T>             The parameter type
         * @return The parameter type factory
         */
        static <A extends CommandActor, T> @NotNull Factory<A> forType(@NotNull Class<T> type, @NotNull ResponseHandler<A, T> responseHandler) {
            return new ClassResponseHandlerFactory<>(type, responseHandler, false);
        }

        /**
         * Creates a new {@link ParameterType.Factory} that returns a {@link ParameterType} for all
         * parameters that are assignable from the given {@code type}.
         * <p>
         * Checking is done using {@link Class#isAssignableFrom(Class)}.
         *
         * @param type            Parameter type to check against
         * @param responseHandler The parameter type to supply
         * @param <A>             The actor type
         * @param <T>             The parameter type
         * @return The parameter type factory
         */
        static <A extends CommandActor, T> @NotNull Factory<A> forTypeAndSubclasses(@NotNull Class<T> type, @NotNull ResponseHandler<A, T> responseHandler) {
            return new ClassResponseHandlerFactory<>(type, responseHandler, true);
        }

        /**
         * Creates a new {@link CommandPermission} for the given type and list of annotations. These
         * annotations will usually represent the ones on the function
         * <p>
         * If this factory is not responsible for the given input, it may return
         * {@code null}.
         *
         * @param annotations The annotation list
         * @param lamp        The {@link Lamp} instance
         * @return The permission
         */
        @Nullable <T> ResponseHandler<A, T> create(@NotNull Type type, @NotNull AnnotationList annotations, @NotNull Lamp<A> lamp);
    }
}

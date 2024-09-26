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
package revxrsal.commands.node;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.Potential;
import revxrsal.commands.util.StackTraceSanitizer;

import static revxrsal.commands.node.DefaultFailureHandler.defaultFailureHandler;
import static revxrsal.commands.util.Preconditions.notNull;

/**
 * Contains the settings for Lamp's underlying dispatcher. This can
 * be used to customize Lamp's behavior for finding and dealing with
 * failed {@link Potential}s that occur by trying out the same input
 * on several commands.
 *
 * @param <A> The actor type
 */
public final class DispatcherSettings<A extends CommandActor> {

    /**
     * The default long format for flags and switches
     */
    public static final String LONG_FORMAT_PREFIX = "--";

    /**
     * The default short format for flags and switches
     */
    public static final String SHORT_FORMAT_PREFIX = "-";

    /**
     * The default number of failed attempts after which Lamp will stop
     * testing out commands (for efficiency) and invoke the {@link FailureHandler}.
     */
    public static final int DEFAULT_MAXIMUM_FAILED_ATTEMPTS = 5;

    /**
     * The number of failed attempts after which Lamp will stop testing out commands
     * (for efficiency) and invoke the {@link #failureHandler()}.
     */
    private final int maximumFailedAttempts;

    /**
     * The failure handler that will consume the failed potentials.
     */
    private final @NotNull FailureHandler<A> failureHandler;

    /**
     * The stack-trace sanitizer. See {@link StackTraceSanitizer}
     */
    private final @NotNull StackTraceSanitizer stackTraceSanitizer;

    private DispatcherSettings(Builder<A> builder) {
        this.maximumFailedAttempts = builder.maximumFailedAttempts;
        this.failureHandler = builder.failureHandler;
        this.stackTraceSanitizer = builder.stackTraceSanitizer;
    }

    /**
     * Creates a new {@link Builder} for constructing {@link DispatcherSettings}
     *
     * @param <A> The actor type
     * @return The newly created builder
     */
    public static @NotNull <A extends CommandActor> Builder<A> builder() {
        return new Builder<>();
    }

    /**
     * The number of failed attempts after which Lamp will stop testing out commands
     * (for efficiency) and invoke the {@link #failureHandler()}.
     *
     * @return the maximum number of failed attempts
     */
    public int maximumFailedAttempts() {
        return maximumFailedAttempts;
    }

    /**
     * The failure handler that will consume the failed potentials.
     *
     * @return the failure handler
     */
    public @NotNull FailureHandler<A> failureHandler() {
        return failureHandler;
    }

    /**
     * The stack trace sanitizer for cleaning up stack-traces of errors
     * thrown inside commands
     *
     * @return The sanitizer
     */
    public @NotNull StackTraceSanitizer stackTraceSanitizer() {
        return stackTraceSanitizer;
    }

    /**
     * Creates a new {@link Builder} based on this {@link DispatcherSettings}
     * instance
     *
     * @return The newly created builder
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Contract(value = "-> new", pure = true)
    public @NotNull Builder<A> toBuilder() {
        return new Builder<>()
                .maximumFailedAttempts(maximumFailedAttempts)
                .failureHandler((FailureHandler) failureHandler);
    }

    /**
     * Builder class for {@link DispatcherSettings}
     *
     * @param <A> The actor type
     */
    public static final class Builder<A extends CommandActor> {

        /**
         * The number of failed attempts after which Lamp will stop testing out commands
         * (for efficiency) and invoke the {@link #failureHandler()}.
         */
        private int maximumFailedAttempts = DEFAULT_MAXIMUM_FAILED_ATTEMPTS;

        /**
         * The failure handler that will consume the failed potentials.
         */
        private FailureHandler<A> failureHandler = defaultFailureHandler();

        /**
         * The stack-trace sanitizer. See {@link StackTraceSanitizer}
         */
        private @NotNull StackTraceSanitizer stackTraceSanitizer = StackTraceSanitizer.defaultSanitizer();

        /**
         * Sets the number of failed attempts after which Lamp will stop testing
         * out commands (for efficiency) and invoke the {@link #failureHandler()}.
         *
         * @param maximumFailedAttempts the maximum number of failed attempts
         * @return This builder
         */
        public @NotNull Builder<A> maximumFailedAttempts(
                @Range(from = 1, to = Integer.MAX_VALUE) int maximumFailedAttempts
        ) {
            if (maximumFailedAttempts < 0)
                throw new IllegalArgumentException("Maximum failed attempts cannot be a negative number!");
            this.maximumFailedAttempts = maximumFailedAttempts;
            return this;
        }

        /**
         * Sets the failure handler that will consume the failed potentials.
         *
         * @param failureHandler the failure handler
         * @return This builder
         */
        @Contract("null -> fail")
        public @NotNull Builder<A> failureHandler(FailureHandler<? super A> failureHandler) {
            notNull(failureHandler, "failure handler");
            //noinspection unchecked
            this.failureHandler = (FailureHandler<A>) failureHandler;
            return this;
        }

        /**
         * Sets the stack-trace sanitizer. See {@link StackTraceSanitizer}
         *
         * @param stackTraceSanitizer Sanitizer to use
         * @return This builder
         */
        public Builder<A> stackTraceSanitizer(@NotNull StackTraceSanitizer stackTraceSanitizer) {
            this.stackTraceSanitizer = notNull(stackTraceSanitizer, "stack trace sanitizer");
            return this;
        }

        /**
         * Creates a new {@link DispatcherSettings} based on this builder
         *
         * @return The newly created dispatcher settings.
         */
        @Contract(value = "-> new", pure = true) public @NotNull DispatcherSettings<A> build() {
            return new DispatcherSettings<>(this);
        }

    }

}

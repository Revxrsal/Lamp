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
     * The failure handler that will consume the failed potentials.
     */
    private final @NotNull FailureHandler<A> failureHandler;

    /**
     * The stack-trace sanitizer. See {@link StackTraceSanitizer}
     */
    private final @NotNull StackTraceSanitizer stackTraceSanitizer;

    /**
     * Whether input left after a command's last argument is an error
     */
    private final boolean rejectTrailingInput;

    private DispatcherSettings(Builder<A> builder) {
        this.failureHandler = builder.failureHandler;
        this.stackTraceSanitizer = builder.stackTraceSanitizer;
        this.rejectTrailingInput = builder.rejectTrailingInput;
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
     * Whether input left after a command's last argument is an error. If
     * {@code false} (the default), {@code /foo bar} runs {@code /foo} when no
     * other command accepts {@code bar}. If {@code true}, it fails with a
     * {@link revxrsal.commands.exception.TrailingInputException}.
     *
     * @return if trailing input is rejected
     */
    public boolean rejectTrailingInput() {
        return rejectTrailingInput;
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
                .failureHandler((FailureHandler) failureHandler)
                .stackTraceSanitizer(stackTraceSanitizer)
                .rejectTrailingInput(rejectTrailingInput);
    }

    /**
     * Builder class for {@link DispatcherSettings}
     *
     * @param <A> The actor type
     */
    public static final class Builder<A extends CommandActor> {

        /**
         * The failure handler that will consume the failed potentials.
         */
        private FailureHandler<A> failureHandler = defaultFailureHandler();

        /**
         * The stack-trace sanitizer. See {@link StackTraceSanitizer}
         */
        private @NotNull StackTraceSanitizer stackTraceSanitizer = StackTraceSanitizer.defaultSanitizer();

        /**
         * Whether input left after a command's last argument is an error
         */
        private boolean rejectTrailingInput = false;

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
         * Sets whether input left after a command's last argument is an error.
         * See {@link DispatcherSettings#rejectTrailingInput()}.
         *
         * @param rejectTrailingInput if trailing input should be rejected
         * @return This builder
         */
        public @NotNull Builder<A> rejectTrailingInput(boolean rejectTrailingInput) {
            this.rejectTrailingInput = rejectTrailingInput;
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

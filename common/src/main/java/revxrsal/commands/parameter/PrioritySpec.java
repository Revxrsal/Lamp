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
package revxrsal.commands.parameter;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Represents a priority specification for a certain {@link ParameterType}.
 * <p>
 * This allows parameters to explicitly mark themselves as of higher
 * priority over other parameters, or of lower priority than others.
 * <p>
 * If a parameter has higher priority, it will be tested first
 * to see if it can take certain input. If it can, parameters
 * of lower priority will not be tested. Otherwise, the parser
 * will move on to the next parameters.
 * <p>
 * It is always advisable to explicitly define some priority for mutually inclusive
 * parameters (i.e. parameters that both may work with the same input), such as
 * an {@code enum} and a {@link String}, or a {@code int} and a {@code double}.
 * Otherwise, Lamp has the right to execute either of them and makes no guarantees
 * whatsoever.
 * <p>
 * Note that it is sufficient to declare priority on a single type. If
 * you declare that type A has higher priority than type B, you do not
 * need to declare that type B has lower priority than type A. Either
 * will suffice.
 * <p>
 * {@link PrioritySpec} can be created using {@link #toBuilder()}.
 *
 * @param comparator The underlying comparator of this priority specification.
 */
public record PrioritySpec(Comparator<ParameterType<?, ?>> comparator) {

    /**
     * @see #defaultPriority()
     */
    private static final PrioritySpec DEFAULT = new PrioritySpec((o1, o2) -> 0);

    /**
     * @see #lowest()
     */
    private static final PrioritySpec LOWEST = new PrioritySpec((o1, o2) -> {
        if (o1.getClass() == o2.getClass())
            return 0;
        return 1;
    });

    /**
     * @see #highest()
     */
    private static final PrioritySpec HIGHEST = new PrioritySpec((o1, o2) -> {
        if (o1.getClass() == o2.getClass())
            return 0;
        return -1;
    });

    /**
     * Creates a new {@link Builder}
     *
     * @return A new builder
     */
    @Contract("-> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Represents a {@link PrioritySpec} that will always come last. This
     * is useful for types that can accept <em>any</em> type
     * of input, such as a {@link StringParameterType}.
     *
     * @return The lowest {@link PrioritySpec} possible.
     */
    public static @NotNull PrioritySpec lowest() {
        return LOWEST;
    }

    /**
     * Represents a {@link PrioritySpec} that will always come first. This
     * is useful for types that can accept a certain, closed set of possible
     * values, such as {@code boolean} or {@code enum} types.
     * <p>
     * You should use this priority sparingly, and avoid it with inputs that
     * do not have known values beforehand, or whose values may change over
     * time.
     * <p>
     * This is best used
     *
     * @return The lowest {@link PrioritySpec} possible.
     */
    public static @NotNull PrioritySpec highest() {
        return HIGHEST;
    }

    /**
     * The default {@link PrioritySpec}, which treats all types with
     * equal priority.
     *
     * @return The default priority.
     */
    public static @NotNull PrioritySpec defaultPriority() {
        return DEFAULT;
    }

    /**
     * Creates a new {@link Builder} from this priority specification
     *
     * @return A new builder based on this priority specification.
     */
    @Contract(pure = true, value = "-> new")
    public @NotNull Builder toBuilder() {
        return new Builder(comparator);
    }

    /**
     * A builder for creating a {@link PrioritySpec}.
     */
    public static class Builder {
        private Comparator<ParameterType<?, ?>> comparator;

        public Builder() {
            this((o1, o2) -> 0);
        }

        public Builder(Comparator<ParameterType<?, ?>> comparator) {
            this.comparator = comparator;
        }

        /**
         * Adds the given comparator. This can be used to add arbitrary
         * comparison techniques
         *
         * @param newComparator The parameter comparator
         * @return This builder instance
         */
        public @NotNull Builder addComparator(@NotNull Comparator<ParameterType<?, ?>> newComparator) {
            this.comparator = newComparator.thenComparing(comparator);
            return this;
        }

        /**
         * Declares this parameter to have higher priority over the specified
         * parameter type
         *
         * @param parameterType Parameter type to come before
         * @return This builder instance
         */
        public @NotNull Builder higherThan(Class<? extends ParameterType<?, ?>> parameterType) {
            Comparator<ParameterType<?, ?>> c = (o1, o2) -> {
                if (parameterType.isAssignableFrom(o2.getClass()))
                    return -1;
                else if (parameterType.isAssignableFrom(o1.getClass()))
                    return 1;
                return 0;
            };
            return addComparator(c);
        }

        /**
         * Declares this parameter to have lower priority than the specified
         * parameter type
         *
         * @param parameterType Parameter type to come after
         * @return This builder instance
         */
        public @NotNull Builder lowerThan(Class<? extends ParameterType<?, ?>> parameterType) {
            Comparator<ParameterType<?, ?>> c = (o1, o2) -> {
                if (parameterType.isAssignableFrom(o2.getClass()))
                    return 1;
                else if (parameterType.isAssignableFrom(o1.getClass()))
                    return -1;
                return 0;
            };
            return addComparator(c);
        }

        /**
         * Builds a {@link PrioritySpec} out of this builder
         *
         * @return The priority spec
         */
        @Contract(value = "-> new", pure = true)
        public @NotNull PrioritySpec build() {
            return new PrioritySpec(comparator);
        }
    }
}

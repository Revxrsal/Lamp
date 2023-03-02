/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copysecond (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copysecond notice and this permission notice shall be included in all
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
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.autocomplete.AutoCompleter;
import revxrsal.commands.core.LinkedArgumentStack;
import revxrsal.commands.util.QuotedStringTokenizer;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

/**
 * Represents a mutable stack of strings represented as command arguments.
 * <p>
 * This class holds extremely similar functionality to a LinkedList, and in
 * most contexts should be safely castable to one.
 */
public interface ArgumentStack extends Deque<String>, List<String>, Cloneable {

    /**
     * Joins all present arguments in this stack
     *
     * @param delimiter Delimiter between these arguments.
     * @return The combined string
     */
    @NotNull String join(String delimiter);

    /**
     * Joins all present arguments in this stack, starting from
     * the specified index
     *
     * @param delimiter  Delimiter between these arguments
     * @param startIndex The start index to combine from
     * @return The combined string
     */
    @NotNull String join(@NotNull String delimiter, int startIndex);

    /**
     * Returns (and removes) the string in which might get concatenated with the rest
     * of the arguments if the parameter {@link CommandParameter#consumesAllString() consumes all strings}
     * that follow it.
     *
     * @param parameter The parameter to get for
     * @return The string for this parameter. Will return the first argument if the parameter cannot
     * consume all strings.
     * @see CommandParameter#consumesAllString()
     */
    String popForParameter(@NotNull CommandParameter parameter);

    /**
     * Returns this argument stack as an immutable view. This can be therefore
     * passed to any conditions or resolvers without having to worry about being
     * unintentionally modified.
     * <p>
     * Note that this does not create an independent copy, and instead returns
     * a view which does not allow modifications. If this argument stack gets
     * modified from somewhere else, the immutable view will also be modified.
     *
     * @return The argument stack as an immutable view
     */
    @NotNull @UnmodifiableView List<String> asImmutableView();

    /**
     * Returns an immutable copy of this stack. This copy will behave
     * independently of the original {@link ArgumentStack}.
     *
     * @return An immutable copy of this {@link ArgumentStack}.
     */
    @NotNull @Unmodifiable List<String> asImmutableCopy();

    /**
     * Returns an independent copy of this argument stack.
     *
     * @return A copy of this argument stack
     */
    @NotNull ArgumentStack copy();

    /**
     * Parses the given input and returns a new {@link ArgumentStack} with
     * the specified arguments. This will respect quotes, backslashes, and
     * other quote-specific grammar.
     *
     * @param arguments Arguments to clone from
     * @return The newly created argument stack.
     */
    static @NotNull ArgumentStack parse(@NotNull String... arguments) {
        if (arguments.length == 0) return empty();
        return new LinkedArgumentStack(QuotedStringTokenizer.parse(String.join(" ", arguments)));
    }

    /**
     * Parses the given input and returns a new {@link ArgumentStack} with
     * the specified arguments. This will respect quotes, backslashes, and
     * other quote-specific grammar.
     *
     * @param arguments Arguments to clone from
     * @return The newly created argument stack.
     */
    static @NotNull ArgumentStack parse(@NotNull Collection<String> arguments) {
        if (arguments.size() == 0) return empty();
        return new LinkedArgumentStack(QuotedStringTokenizer.parse(String.join(" ", arguments)));
    }

    /**
     * Returns a new {@link ArgumentStack} with the specified arguments. This
     * will not remove trailing space, and is dedicated only for usage in
     * tab completions.
     * <p>
     * This should only be used with {@link AutoCompleter#complete(CommandActor, ArgumentStack)}.
     *
     * @param arguments Arguments to clone from
     * @return The newly created argument stack.
     */
    static @NotNull ArgumentStack parseForAutoCompletion(@NotNull String... arguments) {
        return new LinkedArgumentStack(QuotedStringTokenizer.parseForAutoCompletion(String.join(" ", arguments)));
    }

    /**
     * Returns a new {@link ArgumentStack} with the specified arguments. This
     * will not remove trailing space, and is dedicated only for usage in
     * tab completions.
     * <p>
     * This should only be used with {@link AutoCompleter#complete(CommandActor, ArgumentStack)}.
     *
     * @param arguments Arguments to clone from
     * @return The newly created argument stack.
     */
    static @NotNull ArgumentStack parseForAutoCompletion(@NotNull Collection<String> arguments) {
        return new LinkedArgumentStack(QuotedStringTokenizer.parseForAutoCompletion(String.join(" ", arguments)));
    }

    /**
     * Returns a new {@link ArgumentStack} with the specified arguments, without
     * doing any modification to the input. This will not respect quotes or backslashes
     *
     * @param arguments Arguments to clone from
     * @return The newly created argument stack.
     */
    static @NotNull ArgumentStack copyExact(@NotNull String... arguments) {
        if (arguments.length == 0) return empty();
        return new LinkedArgumentStack(arguments);
    }

    /**
     * Returns a new {@link ArgumentStack} with the specified arguments, without
     * doing any modification to the input. This will not respect quotes or backslashes
     *
     * @param arguments Arguments to clone from
     * @return The newly created argument stack.
     */
    static @NotNull ArgumentStack copyExact(@NotNull List<String> arguments) {
        if (arguments.size() == 0) return empty();
        return new LinkedArgumentStack(arguments);
    }

    /**
     * Returns a new, empty {@link ArgumentStack}.
     *
     * @return A new, empty argument stack
     */
    static @NotNull ArgumentStack empty() {
        return new LinkedArgumentStack();
    }

}
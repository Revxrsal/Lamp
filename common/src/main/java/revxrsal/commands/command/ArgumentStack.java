package revxrsal.commands.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.core.LinkedArgumentStack;
import revxrsal.commands.util.tokenize.QuotedStringTokenizer;

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
     * Returns a new {@link ArgumentStack} with the specified arguments.
     *
     * @param arguments Arguments to clone from
     * @return The newly created argument stack.
     */
    static @NotNull ArgumentStack of(@NotNull Collection<String> arguments) {
        if (arguments.size() == 0) return empty();
        return new LinkedArgumentStack(String.join(" ", arguments));
    }

    /**
     * Returns a new {@link ArgumentStack} with the specified arguments, without
     * doing any special parsing for quotes.
     *
     * @param arguments Arguments to clone from
     * @return The newly created argument stack.
     */
    static @NotNull ArgumentStack exactly(@NotNull Collection<String> arguments) {
        if (arguments.size() == 0) return empty();
        return new LinkedArgumentStack(arguments.toArray(new String[0]));
    }

    /**
     * Returns a new {@link ArgumentStack} with the specified arguments.
     *
     * @param arguments Arguments to clone from
     * @return The newly created argument stack.
     */
    static @NotNull ArgumentStack of(@NotNull String... arguments) {
        if (arguments.length == 0) return empty();
        return new LinkedArgumentStack(arguments);
    }

    /**
     * Returns a new {@link ArgumentStack} with the specified arguments,
     * from splitting the string by whitespace.
     *
     * @param arguments Arguments to split
     * @return The newly created argument stack.
     */
    static @NotNull ArgumentStack fromString(@NotNull String arguments) {
        return new LinkedArgumentStack(QuotedStringTokenizer.tokenize(arguments));
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
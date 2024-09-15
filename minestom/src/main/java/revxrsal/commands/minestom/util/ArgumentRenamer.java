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
package revxrsal.commands.minestom.util;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static revxrsal.commands.util.Preconditions.cannotInstantiate;
import static revxrsal.commands.util.Preconditions.notNull;

/**
 * A utility for cloning {@link Argument}s but with different
 * IDs.
 */
public final class ArgumentRenamer {

    private ArgumentRenamer() {
        cannotInstantiate(ArgumentRenamer.class);
    }

    /**
     * Creates a new {@link Argument} that is a clone of the given one, but with
     * the given ID.
     *
     * @param argument Argument to rename
     * @param id       The new ID
     * @param <T>      The argument type
     * @return The renamed argument
     */
    @Contract(pure = true)
    public static <T> @NotNull Argument<T> rename(@NotNull Argument<T> argument, @NotNull String id) {
        notNull(argument, "argument");
        notNull(id, "id");
        return new RenamedArgument<>(id, argument);
    }

    private static final class RenamedArgument<T> extends Argument<T> {
        private final Argument<T> argument;

        private RenamedArgument(@NotNull String newId, @NotNull Argument<T> argument) {
            super(newId, argument.allowSpace(), argument.useRemaining());
            if (argument.getSuggestionCallback() != null)
                this.setSuggestionCallback(argument.getSuggestionCallback());
            if (argument.getDefaultValue() != null)
                this.setDefaultValue(argument.getDefaultValue());
            this.argument = argument;
        }

        @Override
        public @NotNull T parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
            return argument.parse(sender, input);
        }

        @Override
        public String parser() {
            return argument.parser();
        }

        @Override
        public byte @Nullable [] nodeProperties() {
            return argument.nodeProperties();
        }
    }
}

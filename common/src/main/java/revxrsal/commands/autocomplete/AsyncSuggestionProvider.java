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
package revxrsal.commands.autocomplete;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * An interface that supplies completions asynchronously for the user
 * depending on their input.
 * <p>
 * Note: The ability to provide asynchronous completions is platform-dependent.
 * In platforms where such behavior is unsupported, Lamp will fall back to
 * normal completions, and {@link #getSuggestionsAsync(ExecutionContext)} will
 * block.
 */
@FunctionalInterface
public interface AsyncSuggestionProvider<A extends CommandActor> {

    /**
     * Returns the suggestions
     *
     * @param context The execution context. This will try to parse
     *                arguments inputted by the user and store them
     *                to provide context-aware suggestions.
     * @return The command suggestions.
     */
    @NotNull
    CompletableFuture<Collection<String>> getSuggestionsAsync(@NotNull ExecutionContext<A> context);

}

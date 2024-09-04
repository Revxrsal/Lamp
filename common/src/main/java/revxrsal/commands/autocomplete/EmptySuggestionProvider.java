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
package revxrsal.commands.autocomplete;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.stream.StringStream;

import java.util.List;

/**
 * Represents an empty {@link SuggestionProvider}. Accessible using {@link SuggestionProvider#empty()}.
 */
final class EmptySuggestionProvider implements SuggestionProvider<CommandActor> {

    public static final EmptySuggestionProvider INSTANCE = new EmptySuggestionProvider();

    private EmptySuggestionProvider() {
    }

    @Override
    public @NotNull List<String> getSuggestions(@NotNull StringStream input, @NotNull ExecutionContext<CommandActor> context) {
        return List.of();
    }

    @Override
    public String toString() {
        return "SuggestionProvider.empty()";
    }

    @Override public int hashCode() {
        return EmptySuggestionProvider.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EmptySuggestionProvider;
    }
}

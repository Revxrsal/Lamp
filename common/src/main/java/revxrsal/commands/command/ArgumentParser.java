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
package revxrsal.commands.command;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.exception.ArgumentParseException;
import revxrsal.commands.util.Strings;
import revxrsal.commands.util.QuotedStringTokenizer;

/**
 * Represents a parser that receives strings and converts them into
 * collection-like {@link ArgumentStack} that is consumed by resolvers.
 * <p>
 * Argument parsers allow to customize the tokenizing logic of strings,
 * such as allowing quotes, skipping extra whitespace, etc.
 * <p>
 * Set with {@link CommandHandler#setArgumentParser(ArgumentParser)}
 *
 * @deprecated Causes bugs with Brigadier, and has no real-world use-cases.
 */
@FunctionalInterface
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "3.1.4")
public interface ArgumentParser {

    /**
     * An argument parser that parses strings by quotes and skips
     * extra whitespace.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "3.1.4")
    ArgumentParser QUOTES = QuotedStringTokenizer.INSTANCE;

    /**
     * An argument parser that only parses strings by spaces,
     * and does not respect quotes.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "3.1.4")
    ArgumentParser NO_QUOTES = arguments -> ArgumentStack.copy(Strings.SPACE.split(arguments));

    /**
     * Parses the string and returns an {@link ArgumentStack} for it.
     *
     * @param arguments String to parse. This string is guaranteed never
     *                  to be null or empty.
     * @return The argument stack. You should create this with {@link ArgumentStack#empty()}
     * or other methods
     * @throws ArgumentParseException An exception to throw in case of errors while parsing
     *                                the string. It is optional to throw this.
     */
    ArgumentStack parse(@NotNull String arguments) throws ArgumentParseException;

}

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
package revxrsal.commands.stream.token;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.stream.MutableStringStream;

/**
 * A utility class for parsing {@link Token}s in annotations
 */
@ApiStatus.Internal
public final class TokenParser {

    /**
     * Parses the next {@link Token} in the given stream
     *
     * @param stream The stream to parse
     * @return The parsed token
     * @throws ParseException if an exception occurs during parsing
     */
    @Contract(mutates = "param1")
    public static @NotNull Token parseNextToken(@NotNull MutableStringStream stream) {
        if (stream.peek() == '<') {
            stream.moveForward();
            String name = stream.readUntil('>');
            if (name.isEmpty())
                throw new ParseException("Cannot have <> for an argument name!");
            return new ParameterToken(name);
        }
        String name = stream.readUnquotedString();
        return new LiteralToken(name);
    }

    public static class ParseException extends RuntimeException {

        public ParseException(String message) {
            super(message);
        }
    }
}

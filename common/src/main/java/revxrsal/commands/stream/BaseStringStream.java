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
package revxrsal.commands.stream;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.exception.InputParseException;

/**
 * A class that aids in parsing a stream of characters.
 */
class BaseStringStream implements StringStream {

    /**
     * The escape character
     */
    protected static final char ESCAPE = '\\';

    /**
     * The character for quoting
     */
    protected static final char DOUBLE_QUOTE = '"';

    /**
     * The source string being read
     */
    protected String source;

    /**
     * The current reading position
     */
    protected int pos;

    /**
     * Creates a new {@link MutableStringStream} with its position at zero.
     *
     * @param source Source to read
     */
    BaseStringStream(@NotNull String source) {
        this(source, 0);
    }

    /**
     * Creates a new {@link MutableStringStream} with its position at zero.
     *
     * @param source Source to read
     */
    BaseStringStream(@NotNull String source, int position) {
        this.source = source;
        this.pos = position;
    }

    @Override
    public @NotNull String source() {
        return source;
    }

    public int totalSize() {
        return source.length();
    }

    @Override
    public int remaining() {
        return source.length() - pos;
    }

    @Contract(pure = true)
    public char peek() {
        return source.charAt(pos);
    }

    @Override
    public String peek(int characters) {
        if (!canRead(characters))
            return peekRemaining();
        return source.substring(pos, pos + characters);
    }

    @Contract(pure = true)
    public char peekOffset(int offset) {
        return source.charAt(pos + offset);
    }

    public boolean hasRemaining() {
        return canRead(1);
    }

    public boolean hasFinished() {
        return !hasRemaining();
    }

    public boolean canRead(int characters) {
        return pos + characters <= source.length();
    }

    public int position() {
        return pos;
    }

    public @NotNull String readUnquotedString() {
        int start = pos;
        while (hasRemaining() && !Character.isWhitespace(peek())) {
            pos += 1;
        }
        return source.substring(start, pos);
    }

    public @NotNull String readString() {
        if (!hasRemaining())
            return "";
        char next = peek();
        if (next == DOUBLE_QUOTE) {
            pos += 1;
            return readUntil(DOUBLE_QUOTE);
        }
        return readUnquotedString();
    }

    public char read() {
        return source.charAt(pos++);
    }

    public @NotNull String readUntil(char delimiter) {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        while (hasRemaining()) {
            char c = read();
            if (escaped) {
                if (c == delimiter || c == ESCAPE) {
                    result.append(c);
                    escaped = false;
                } else {
                    pos--;
                    throw new InputParseException(InputParseException.Cause.INVALID_ESCAPE_CHARACTER);
                }
            } else if (c == ESCAPE) {
                escaped = true;
            } else if (c == delimiter) {
                return result.toString();
            } else {
                result.append(c);
            }
        }
        throw new InputParseException(InputParseException.Cause.UNCLOSED_QUOTE);
    }

    @Override public @NotNull String peekString() {
        int cursor = pos;
        String value = readString();
        pos = cursor;
        return value;
    }

    public @NotNull String peekUnquotedString() {
        int cursor = pos;
        String value = readUnquotedString();
        pos = cursor;
        return value;
    }

    public @NotNull String peekRemaining() {
        if (hasFinished())
            return "";
        return source.substring(pos);
    }

    @Override
    public @NotNull @Unmodifiable StringStream toImmutableCopy() {
        return new BaseStringStream(source, pos);
    }

    @Override
    public @NotNull @Contract(value = "-> new", pure = true) MutableStringStream toMutableCopy() {
        return new MutableStringStreamImpl(source, pos);
    }

    /**
     * Tests whether is this stream mutable or not
     *
     * @return If this stream is mutable or not
     */
    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return source.isEmpty();
    }
}

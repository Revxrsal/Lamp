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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.InputParseException;
import revxrsal.commands.exception.InvalidBooleanException;
import revxrsal.commands.exception.InvalidDecimalException;
import revxrsal.commands.exception.InvalidIntegerException;

import java.util.Locale;

public final class MutableStringStreamImpl extends BaseStringStream implements MutableStringStream {

    /**
     * Creates a new {@link MutableStringStream} with its position at zero.
     *
     * @param source Source to read
     */
    MutableStringStreamImpl(String source) {
        super(source);
    }

    /**
     * Creates a new {@link MutableStringStream} with its position at zero.
     *
     * @param source   Source to read
     * @param position Cursor position
     */
    MutableStringStreamImpl(String source, int position) {
        super(source, position);
    }

    @Override
    public char read() {
        return source.charAt(pos++);
    }

    @Override
    public String read(int characters) {
        if (!canRead(characters))
            return consumeRemaining();
        return source.substring(pos, pos += characters);
    }

    public void moveForward() {
        moveForward(1);
    }

    public void moveBackward() {
        moveBackward(1);
    }

    public void moveForward(int by) {
        pos += by;
    }

    public void moveBackward(int by) {
        pos = Math.max(0, pos - by);
    }

    public @NotNull String consumeRemaining() {
        if (hasFinished())
            return "";
        String v = source.substring(pos);
        skipToEnd();
        return v;
    }

    public void skipToEnd() {
        pos = source.length();
    }

    @ApiStatus.Internal
    public void extend(@NotNull String str) {
        source += str;
    }

    public @Override @NotNull String readUnquotedString() {
        return super.readUnquotedString();
    }

    public @NotNull String readString() {
        if (!hasRemaining())
            return "";
        char next = peek();
        if (next == DOUBLE_QUOTE) {
            moveForward();
            return readUntil(DOUBLE_QUOTE);
        }
        return readUnquotedString();
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
                    moveBackward(1);
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

    public @NotNull String readWhile(CharPredicate predicate) {
        int start = pos;
        while (hasRemaining() && predicate.test(peek())) {
            moveForward();
        }
        return source.substring(start, pos);
    }

    private static boolean isNumerical(char c, boolean decimal) {
        boolean isDigit = c >= '0' && c <= '9';
        return decimal ? (isDigit || c == '.') : isDigit;
    }

    public float readFloat() {
        int start = pos;
        String value = readWhile(c -> isNumerical(c, true));
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            pos = start;
            throw new InvalidDecimalException(value);
        }
    }

    public double readDouble() {
        int start = pos;
        String value = readWhile(c -> isNumerical(c, true));
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            pos = start;
            throw new InvalidDecimalException(value);
        }
    }

    public int readInt() {
        int start = pos;
        String value = readWhile(c -> isNumerical(c, false));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            pos = start;
            throw new InvalidIntegerException(value);
        }
    }

    public long readLong() {
        int start = pos;
        String value = readWhile(c -> isNumerical(c, false));
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            pos = start;
            throw new InvalidIntegerException(value);
        }
    }

    public short readShort() {
        int start = pos;
        String value = readWhile(c -> isNumerical(c, false));
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            pos = start;
            throw new InvalidIntegerException(value);
        }
    }

    public byte readByte() {
        int start = pos;
        String value = readWhile(c -> isNumerical(c, false));
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
            pos = start;
            throw new InvalidIntegerException(value);
        }
    }

    public boolean readBoolean() {
        int start = pos;
        String value = readString();
        return switch (value.toLowerCase(Locale.ENGLISH)) {
            case "true", "yes" -> true;
            case "false", "no", "nope" -> false;
            default -> {
                pos = start;
                throw new InvalidBooleanException(value);
            }
        };
    }

    public void setPosition(int pos) {
        this.pos = pos;
    }

    /**
     * Tests whether is this stream mutable or not
     *
     * @return If this stream is mutable or not
     */
    @Override
    public boolean isMutable() {
        return true;
    }

}

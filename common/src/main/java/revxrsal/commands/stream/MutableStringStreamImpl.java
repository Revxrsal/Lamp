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
    public String read(int characters) {
        if (!canRead(characters))
            return consumeRemaining();
        return source.substring(pos, pos += characters);
    }

    public void moveForward() {
        moveForward(1);
    }

    @Override public void skipWhitespace() {
        while (hasRemaining() && peek() == ' ')
            moveForward();
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

    public float readFloat() {
        String value = readUnquotedString();
        try {
            float v = Float.parseFloat(value);
            if (Float.isFinite(v)) return v;
            throw new InvalidDecimalException(value);
        } catch (NumberFormatException e) {
            throw new InvalidDecimalException(value);
        }
    }

    public double readDouble() {
        String value = readUnquotedString();
        try {
            double v = Double.parseDouble(value);
            if (Double.isFinite(v)) return v;
            throw new InvalidDecimalException(value);
        } catch (NumberFormatException e) {
            throw new InvalidDecimalException(value);
        }
    }

    public int readInt() {
        String value = readUnquotedString();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidIntegerException(value);
        }
    }

    public long readLong() {
        String value = readUnquotedString();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new InvalidIntegerException(value);
        }
    }

    public short readShort() {
        String value = readUnquotedString();
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw new InvalidIntegerException(value);
        }
    }

    public byte readByte() {
        String value = readUnquotedString();
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
            throw new InvalidIntegerException(value);
        }
    }

    public boolean readBoolean() {
        String value = readString();
        switch (value.toLowerCase(Locale.ENGLISH)) {
            case "true":
            case "yes":
                return true;
            case "false":
            case "no":
            case "nope":
                return false;
            default:
                throw new InvalidBooleanException(value);
        }
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

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
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.exception.InputParseException;
import revxrsal.commands.exception.InvalidBooleanException;
import revxrsal.commands.exception.InvalidDecimalException;
import revxrsal.commands.exception.InvalidIntegerException;

import java.util.Locale;

public final class MutableStringStreamImpl extends BaseStringStream implements MutableStringStream {

    private StringStreamView immutableView = null;

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
        if (!containsDigit(value))
            throw new InvalidDecimalException(value);
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
        if (!containsDigit(value))
            throw new InvalidDecimalException(value);
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
        if (!containsDigit(value))
            throw new InvalidIntegerException(value);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidIntegerException(value);
        }
    }

    public long readLong() {
        String value = readUnquotedString();
        if (!containsDigit(value))
            throw new InvalidIntegerException(value);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new InvalidIntegerException(value);
        }
    }

    public short readShort() {
        String value = readUnquotedString();
        if (!containsDigit(value))
            throw new InvalidIntegerException(value);
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw new InvalidIntegerException(value);
        }
    }

    public byte readByte() {
        String value = readUnquotedString();
        if (!containsDigit(value))
            throw new InvalidIntegerException(value);
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
            throw new InvalidIntegerException(value);
        }
    }

    /**
     * Every valid number contains a digit. Checking this first avoids an
     * expensive NumberFormatException for words, which is common when
     * overloads are tried out.
     */
    private static boolean containsDigit(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i)))
                return true;
        }
        return false;
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

    @Override public @NotNull @Unmodifiable StringStream toImmutableView() {
        if (immutableView == null)
            immutableView = new StringStreamView();
        return immutableView;
    }

    private class StringStreamView implements StringStream {

        @Override public @NotNull String source() {
            return MutableStringStreamImpl.this.source();
        }

        @Override public int totalSize() {
            return MutableStringStreamImpl.this.totalSize();
        }

        @Override public int remaining() {
            return MutableStringStreamImpl.this.remaining();
        }

        @Override public char peek() {
            return MutableStringStreamImpl.this.peek();
        }

        @Override public String peek(int characters) {
            return MutableStringStreamImpl.this.peek(characters);
        }

        @Override public char peekOffset(int offset) {
            return MutableStringStreamImpl.this.peekOffset(offset);
        }

        @Override public boolean hasRemaining() {
            return MutableStringStreamImpl.this.hasRemaining();
        }

        @Override public boolean hasFinished() {
            return MutableStringStreamImpl.this.hasFinished();
        }

        @Override public boolean canRead(int characters) {
            return MutableStringStreamImpl.this.canRead(characters);
        }

        @Override public int position() {
            return MutableStringStreamImpl.this.position();
        }

        @Override public @NotNull String peekUnquotedString() {
            return MutableStringStreamImpl.this.peekUnquotedString();
        }

        @Override public @NotNull String peekString() {
            return MutableStringStreamImpl.this.peekString();
        }

        @Override public @NotNull String peekRemaining() {
            return MutableStringStreamImpl.this.peekRemaining();
        }

        @Override public @NotNull @Unmodifiable StringStream toImmutableCopy() {
            return MutableStringStreamImpl.this.toImmutableCopy();
        }

        @Override public @NotNull MutableStringStream toMutableCopy() {
            return MutableStringStreamImpl.this.toMutableCopy();
        }

        @Override public boolean isMutable() {
            return false;
        }

        @Override public boolean isEmpty() {
            return MutableStringStreamImpl.this.isEmpty();
        }
    }

}

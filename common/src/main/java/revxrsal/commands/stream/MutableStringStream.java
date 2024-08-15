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

/**
 * An interface that aids in parsing a stream of characters.
 */
public interface MutableStringStream extends StringStream {

    /**
     * Reads the next character, and moves the cursor forward.
     *
     * @return The next character
     */
    char read();

    /**
     * Reads the given number of characters, and moves the cursor forward.
     *
     * @return The number of characters to read
     */
    String read(int characters);

    /**
     * Moves the cursor 1 step forward
     */
    void moveForward();

    /**
     * Moves the cursor 1 step backward
     */
    void moveBackward();

    /**
     * Moves the cursor by the given steps forward
     *
     * @param by Steps to move
     */
    void moveForward(int by);

    /**
     * Moves the cursor by the given steps backward
     *
     * @param by Steps to move
     */
    void moveBackward(int by);

    /**
     * Consumes the rest of the stream
     *
     * @return The remaining string
     */
    @NotNull
    String consumeRemaining();

    /**
     * Skips to the end of the stream
     */
    void skipToEnd();

    /**
     * Reads a single, unquoted string. This will simply move forward until it encounters
     * a whitespace character.
     *
     * @return The next string
     */
    @NotNull
    String readUnquotedString();

    /**
     * Reads the next string. If the string was quoted, it will
     * read the entire string inside the quotes. Otherwise, it will
     * consume the next string until a whitespace character is
     * encountered.
     *
     * @return The next string.
     */
    @NotNull
    String readString();

    /**
     * Reads a string until it encounters the given delimiter character. This
     * method will respect escaping backslashes that come before a {@code delimiter}
     * or before another backslash.
     *
     * @param delimiter Delimiter character
     * @return The read string
     */
    @NotNull
    String readUntil(char delimiter);

    /**
     * Reads from the text stream until the given predicate returns
     * {@code false}.
     *
     * @param predicate Predicate to test with
     * @return The read string.
     */
    @NotNull
    String readWhile(CharPredicate predicate);

    /**
     * Parses the next token as a {@code float}.
     *
     * @return The next token, as a {@code float}.
     */
    float readFloat();

    /**
     * Parses the next token as a {@code double}.
     *
     * @return The next token, as a {@code double}.
     */
    double readDouble();

    /**
     * Parses the next token as an {@code int}.
     *
     * @return The next token, as an {@code int}.
     */
    int readInt();

    /**
     * Parses the next token as a {@code long}.
     *
     * @return The next token, as a {@code long}.
     */
    long readLong();

    /**
     * Parses the next token as a {@code short}.
     *
     * @return The next token, as a {@code short}.
     */
    short readShort();

    /**
     * Parses the next token as a {@code byte}.
     *
     * @return The next token, as a {@code byte}.
     */
    byte readByte();

    /**
     * Parses the next token as a {@code boolean}.
     *
     * @return The next token, as a {@code boolean}.
     */
    boolean readBoolean();

    /**
     * Sets the current position of the cursor
     *
     * @param position The cursor position
     */
    void setPosition(int position);

    /**
     * Returns an {@link StringStream} copy of this string stream
     *
     * @return an immutable copy of this.
     */
    @NotNull
    @Contract(pure = true, value = "-> new")
    @Unmodifiable
    StringStream toImmutableCopy();

    /**
     * Returns a new, mutable {@link MutableStringStream} copy of this stream.
     *
     * @return The new string stream
     */
    @NotNull
    @Contract(pure = true, value = "-> new")
    MutableStringStream toMutableCopy();

}

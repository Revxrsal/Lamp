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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public interface StringStream {

    /**
     * Returns an immutable {@link StringStream} with the given source and
     * cursor position
     *
     * @param source   The source string to work with
     * @param position The cursor position
     * @return The newly created {@link StringStream}
     */
    static @NotNull StringStream create(@NotNull String source, int position) {
        return new BaseStringStream(source, position);
    }

    /**
     * Returns an immutable {@link StringStream} with the given source, starting
     * at the beginning of the string.
     *
     * @param source The source string to work with
     * @return The newly created {@link StringStream}
     */
    static @NotNull StringStream create(@NotNull String source) {
        return new BaseStringStream(source);
    }

    /**
     * Returns a mutable {@link MutableStringStream} with the given source and
     * cursor position
     *
     * @param source   The source string to work with
     * @param position The cursor position
     * @return The newly created {@link MutableStringStream}
     */
    static @NotNull MutableStringStream createMutable(@NotNull String source, int position) {
        return new MutableStringStreamImpl(source, position);
    }

    /**
     * Returns a mutable {@link MutableStringStream} with the given source, starting
     * at the beginning of the string.
     *
     * @param source The source string to work with
     * @return The newly created {@link MutableStringStream}
     */
    static @NotNull MutableStringStream createMutable(@NotNull String source) {
        return new MutableStringStreamImpl(source);
    }

    /**
     * Returns the original entire string source.
     *
     * @return The string source
     */
    @NotNull
    String source();

    /**
     * Returns the size of the source
     *
     * @return The source size
     */
    int totalSize();

    /**
     * Returns the characters remaining
     *
     * @return The number of characters remaining
     */
    int remaining();

    /**
     * Peeks the next character, without moving the cursor forward.
     *
     * @return The next character
     */
    char peek();

    /**
     * Peeks the next number of character, without moving the cursor forward.
     *
     * @return The number of characters to peek
     */
    String peek(int characters);

    /**
     * Peeks the character after {@code offset} chars, without moving the cursor forward.
     *
     * @param offset Characters to peek
     * @return The next character
     */
    char peekOffset(int offset);

    /**
     * Tests whether there are any characters left in the string
     * stream
     *
     * @return if there are any characters left
     */
    boolean hasRemaining();

    /**
     * Tests whether this stream has finished parsing or not
     *
     * @return If the stream has finished or not
     */
    boolean hasFinished();

    /**
     * Tests whether the stream can read the given {@code characters} count
     *
     * @param characters Number of characters
     * @return if such a number can be read or not.
     */
    boolean canRead(int characters);

    /**
     * Returns the cursor position
     *
     * @return The cursor position
     */
    int position();

    /**
     * Peeks a single, unquoted string. This will simply move forward until it encounters
     * a whitespace character.
     * <p>
     * This will not move the cursor forward.
     *
     * @return The peeked string
     */
    @NotNull
    String peekUnquotedString();

    /**
     * Peeks the next string. If the string was quoted, it will
     * peek the entire string inside the quotes. Otherwise, it will
     * peek the next string until a whitespace character is encountered.
     * <p>
     * This will not move the cursor forward.
     *
     * @return The next string.
     */
    @NotNull
    String peekString();

    /**
     * Peeks the remainder of the string stream
     *
     * @return The peeked string
     */
    @NotNull
    String peekRemaining();

    /**
     * Returns an {@link StringStream} copy of this string stream
     *
     * @return an immutable copy of this.
     */
    @NotNull
    @Unmodifiable
    StringStream toImmutableCopy();

    /**
     * Returns a new, mutable {@link MutableStringStream} copy of this stream.
     *
     * @return The new string stream
     */
    @NotNull
    MutableStringStream toMutableCopy();

    /**
     * Tests whether is this stream mutable or not
     *
     * @return If this stream is mutable or not
     */
    boolean isMutable();

    /**
     * Tests whether is this stream empty or not.
     * <p>
     * This is equivalent to {@link String#isEmpty()}
     *
     * @return If this stream empty or not
     */
    boolean isEmpty();
}

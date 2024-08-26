/*
 * This file is part of sweeper, licensed under the MIT License.
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
package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.stream.MutableStringStream;

/**
 * Thrown when a {@link MutableStringStream} fails to parse the user input. This
 * occurs if the user inputs an invalid escape sequence, or if they have
 * unclosed quotations
 */
@ThrowableFromCommand
public final class InputParseException extends RuntimeException {

    /**
     * The input parse cause
     */
    private final @NotNull Cause cause;

    public InputParseException(@NotNull Cause cause) {
        this.cause = cause;
    }

    /**
     * Returns the cause of the exception
     *
     * @return The parse error
     */
    public @NotNull Cause cause() {
        return cause;
    }

    public enum Cause {
        INVALID_ESCAPE_CHARACTER,
        UNCLOSED_QUOTE,
        EXPECTED_WHITESPACE
    }

}

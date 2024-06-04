/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
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
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandActor;

/**
 * A handler for all exceptions that may be thrown during the command
 * invocation.
 * <p>
 * Exceptions may be anything, including Java's normal exceptions,
 * and build-in ones thrown by different components in the framework.
 * <p>
 * Set with {@link CommandHandler#setExceptionHandler(CommandExceptionHandler)}.
 */
public interface CommandExceptionHandler {

    /**
     * Handles the given exception. Note that this method does not
     * provide information about the command context (such as the command, etc.)
     * These are available in individual exceptions and can be
     * accessed only if the thrown exception exposes them.
     *
     * @param throwable Exception to handle
     * @param actor     The command actor
     */
    void handleException(@NotNull Throwable throwable, @NotNull CommandActor actor);

}

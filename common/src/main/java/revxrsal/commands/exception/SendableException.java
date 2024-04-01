/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copysecond (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copysecond notice and this permission notice shall be included in all
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
import revxrsal.commands.command.CommandActor;

/**
 * Represents an exception that is purely used to send messages directly to
 * the sender. Exceptions of this type are <strong>not</strong> handled by
 * exception handlers, and instead get their {@link #sendTo(CommandActor)} method invoked
 * directly.
 */
@ThrowableFromCommand
public abstract class SendableException extends RuntimeException {

    /**
     * Constructs a new {@link SendableException} that does not send any message.
     * <p>
     * Use this constructor if you would like to implement your own messaging
     * system instead of relying on {@link CommandActor#reply(String)}.
     */
    public SendableException() {
        this("");
    }

    /**
     * Constructs a new {@link SendableException} with an inferred actor
     *
     * @param message Message to send
     */
    public SendableException(String message) {
        super(message);
    }

    /**
     * Sends the message to the given actor
     *
     * @param actor Actor to send to
     */
    public abstract void sendTo(@NotNull CommandActor actor);

}

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
 * Used to directly send-and-return a message to the command actor.
 * <p>
 * This exception should be used to directly transfer messages to the {@link CommandActor},
 * however should not be used in command-fail contexts. To signal an error to the
 * actor, use {@link CommandErrorException}.
 */
public class SendMessageException extends SendableException {

    /**
     * Constructs a new {@link SendMessageException} that does not send any message.
     * <p>
     * Use this constructor if you would like to implement your own messaging
     * system instead of relying on {@link CommandActor#reply(String)}.
     */
    public SendMessageException() {
        super();
    }

    /**
     * Constructs a new {@link SendMessageException} with an inferred actor
     *
     * @param message Message to send
     */
    public SendMessageException(String message) {
        super(message);
    }

    /**
     * Sends the message to the given actor
     *
     * @param actor Actor to send to
     */
    @Override public void sendTo(@NotNull CommandActor actor) {
        if (getMessage().isEmpty())
            return;
        actor.reply(getMessage());
    }
}

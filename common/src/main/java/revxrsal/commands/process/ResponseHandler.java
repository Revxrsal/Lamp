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
package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;

/**
 * A handler for post-handling command responses (results returned from the
 * command methods)
 *
 * @param <T> The response type
 */
public interface ResponseHandler<T> {

    /**
     * Handles the response returned from the method
     *
     * @param response The response returned from the method. May or may
     *                 not be null.
     * @param actor    The actor of the command
     * @param command  The command being executed
     */
    void handleResponse(T response, @NotNull CommandActor actor, @NotNull ExecutableCommand command);

    /**
     * A utility method that directly replies with the response.
     * <p>
     * This is intended to be used as a method reference: {@code ResponseHandler::reply}
     *
     * @param response Response to handle
     * @param actor    The command actor
     * @param command  The command being executed
     */
    static void reply(Object response, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
        actor.reply(String.valueOf(response));
    }
}

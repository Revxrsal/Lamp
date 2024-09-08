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
package revxrsal.commands.command;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.util.UUID;

/**
 * Represents a command sender, responsible for performing a command-related
 * action.
 */
public interface CommandActor {

    /**
     * Returns the name of this actor. Varies depending on the
     * platform.
     *
     * @return The actor name
     */
    @NotNull String name();

    /**
     * Returns the unique UID of this subject. Varies depending
     * on the platform.
     * <p>
     * Although some platforms explicitly have their underlying senders
     * have UUIDs, some platforms may have to generate this UUID based on other available
     * data.
     *
     * @return The UUID of this subject.
     */
    @NotNull UUID uniqueId();

    /**
     * Replies to the sender with the specified message.
     * <p>
     * If the {@link Lamp} instance provides a custom {@link Lamp#messageSender()},
     * it will be used, otherwise this will fall back to the normal sender.
     * <p>
     * Varies depending on the platform.
     *
     * @param message Message to reply with.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    default void reply(@NotNull String message) {
        MessageSender messageSender = lamp().messageSender();
        messageSender.send(this, message);
    }

    /**
     * Sends a message to the sender. This implementation
     * is specific to the platform.
     *
     * @param message Message to send
     */
    void sendRawMessage(@NotNull String message);

    /**
     * Replies to the sender with the specified message, and marks it as
     * an error depending on the platform.
     * <p>
     * If the {@link Lamp} instance provides a custom {@link Lamp#errorSender()},
     * it will be used, otherwise this will fall back to the normal sender.
     * <p>
     * Note that, in certain platforms where no "error" mode is available,
     * this may effectively be equivalent to calling {@link #reply(String)}.
     * <p>
     * This method should not throw any exceptions.
     *
     * @param message Message to reply with
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    default void error(@NotNull String message) {
        MessageSender messageSender = lamp().errorSender();
        messageSender.send(this, message);
    }

    /**
     * Sends an error message to the sender. This implementation
     * is specific to the platform.
     *
     * @param message Error message to send
     */
    void sendRawError(@NotNull String message);

    /**
     * Returns the {@link Lamp} instance that constructed this actor.
     *
     * @return The lamp instance
     * @apiNote Implementors of {@link CommandActor} should override
     * the generics of this method to provide better information about
     * the actual actor type.
     */
    Lamp<?> lamp();

}

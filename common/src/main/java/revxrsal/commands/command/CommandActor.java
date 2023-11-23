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
package revxrsal.commands.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.locales.Translator;

import java.util.Locale;
import java.util.Optional;
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
    @NotNull String getName();

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
    @NotNull UUID getUniqueId();

    /**
     * Replies to the sender with the specified message.
     * <p>
     * Varies depending on the platform.
     *
     * @param message Message to reply with.
     */
    void reply(@NotNull String message);

    /**
     * Replies to the sender with the specified message, and marks it as
     * an error depending on the platform.
     * <p>
     * Note that, in certain platforms where no "error" mode is available,
     * this may effectively be equivilent to calling {@link #reply(String)}.
     * <p>
     * This method should not throw any exceptions.
     *
     * @param message Message to reply with
     */
    void error(@NotNull String message);

    /**
     * Returns the command handler that constructed this actor
     *
     * @return The command handler
     */
    CommandHandler getCommandHandler();

    /**
     * Shortcut to {@link CommandHandler#getTranslator()}
     *
     * @return The command handler translator
     */
    default Translator getTranslator() {
        return getCommandHandler().getTranslator();
    }

    /**
     * Returns the locale of this command actor. This can be used by
     * translation tools to provide specialized messages.
     * <p>
     * Note that platforms that do not support per-actor locales
     * will return a default locale, mostly {@link Locale#ENGLISH}.
     *
     * @return The actor's locale
     */
    default @NotNull Locale getLocale() {
        return getTranslator().getLocale();
    }

    /**
     * Replies with the given message
     *
     * @param key  Key of the message
     * @param args The arguments to format with
     */
    default void replyLocalized(@NotNull String key, Object... args) {
        getTranslator().reply(this, key, getLocale(), args);
    }

    /**
     * Replies with the given message
     *
     * @param key  Key of the message
     * @param args The arguments to format with
     */
    default void errorLocalized(@NotNull String key, Object... args) {
        getTranslator().error(this, key, getLocale(), args);
    }

    /**
     * Evaluates the command from the given arguments
     *
     * @param input Input to invoke
     * @return The result returned from invoking the command method. The
     * optional value may be null if an exception was thrown.
     */
    default <T> Optional<@Nullable T> dispatch(@NotNull String input) {
        return getCommandHandler().dispatch(this, input);
    }

    /**
     * Evaluates the command from the given arguments
     *
     * @param input Input to invoke
     * @return The result returned from invoking the command method. The
     * optional value may be null if an exception was thrown.
     */
    default <T> Optional<@Nullable T> dispatch(@NotNull ArgumentStack input) {
        return getCommandHandler().dispatch(this, input);
    }

    /**
     * Returns this actor as the specified type. This is effectively
     * casting this actor to the given type.
     *
     * @param type Type to cast to
     * @param <T>  The actor type
     * @return This actor but casted.
     */
    default <T extends CommandActor> T as(@NotNull Class<T> type) {
        return type.cast(this);
    }

}

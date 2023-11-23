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
package revxrsal.commands.locales;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * A locale reader is a source in which plain messages for a specific locale
 * are fetched. This can be a file, a resource bundle, or even a remote
 * source.
 */
public interface LocaleReader extends DynamicLocaleReader<String> {

    @Override default void reply(CommandActor actor, String message) {
        actor.reply(message);
    }

    @Override default void error(CommandActor actor, String message) {
        actor.error(message);
    }

    @Override default String format(String message, Object... args) {
        return MessageFormat.format(message, args);
    }

    /**
     * Wraps a {@link ResourceBundle} in a {@link LocaleReader}.
     *
     * @param bundle Bundle to wrap
     * @return The locale reader
     */
    static @NotNull LocaleReader wrap(@NotNull ResourceBundle bundle) {
        return new ResourceBundleLocaleReader(bundle);
    }
}

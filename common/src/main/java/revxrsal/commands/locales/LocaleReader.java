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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A locale reader is a source in which messages for a specific locale
 * are fetched. This can be a file, a resource bundle, or even a remote
 * source.
 */
public interface LocaleReader {

    /**
     * Returns whether this reader contains a mapping for the
     * given key.
     *
     * @param key Key to check for
     * @return {@code true} if this reader has a mapping for the key
     */
    boolean containsKey(String key);

    /**
     * Returns the mapping value for this key. It is recommended that
     * this only return values if {@link #containsKey(String)} is true,
     * otherwise throwing an exception to avoid confusion.
     *
     * @param key Key to fetch for
     * @return The string value
     */
    String get(String key);

    /**
     * Returns the locale of by this reader
     *
     * @return The reader's locale
     */
    Locale getLocale();

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

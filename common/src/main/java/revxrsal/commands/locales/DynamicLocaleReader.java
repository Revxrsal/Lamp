package revxrsal.commands.locales;

import revxrsal.commands.command.CommandActor;

import java.util.Locale;

/**
 * A locale reader is a source in which custom typed messages for a specific locale
 * are fetched. This can be a file, a resource bundle, or even a remote
 * source.
 */
public interface DynamicLocaleReader<T> extends LocaleMessageTransmitter<T> {

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

    T get(String key);

    /**
     * Returns the locale of by this reader
     *
     * @return The reader's locale
     */
    Locale getLocale();

    default void reply(CommandActor actor, String key, Object... args) {
        reply(actor, format(get(key), args));
    }

    default void error(CommandActor actor, String key, Object... args) {
        error(actor, format(get(key), args));
    }
}

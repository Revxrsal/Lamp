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

import java.util.*;

import static revxrsal.commands.util.Preconditions.notNull;

final class SimpleTranslator implements Translator {

    private final Map<Locale, LinkedList<DynamicLocaleReader<?>>> registeredBundles = new HashMap<>();
    private volatile Locale locale = Locale.ENGLISH;

    SimpleTranslator() {
        addResourceBundle("lamp");
        if (classExists("org.bukkit.Bukkit"))
            addResourceBundle("lamp-bukkit");
        if (classExists("org.spongepowered.api.Sponge"))
            addResourceBundle("lamp-sponge");
        if (classExists("com.velocitypowered.api.proxy.ProxyServer"))
            addResourceBundle("lamp-velocity");
        if (classExists("net.md_5.bungee.api.ProxyServer"))
            addResourceBundle("lamp-bungee");
        if (classExists("net.dv8tion.jda.api.JDA"))
            addResourceBundle("lamp-jda");
    }

    @Override public void reply(@NotNull CommandActor actor, @NotNull String key, @NotNull Locale locale, Object... args) {
        notNull(actor, "actor");
        notNull(key, "key");
        notNull(locale, "locale");
        notNull(args, "args");
        DynamicLocaleReader<?> localeReader = findDynamicLocaleReader(key, locale)
                .orElseGet(() -> findDynamicLocaleReader(key, this.locale).orElse(null));
        if (localeReader == null) {
            actor.reply(key);
            return;
        }
        localeReader.reply(actor, key, args);
    }

    @Override public void error(@NotNull CommandActor actor, @NotNull String key, @NotNull Locale locale, Object... args) {
        notNull(actor, "actor");
        notNull(key, "key");
        notNull(locale, "locale");
        notNull(args, "args");
        DynamicLocaleReader<?> localeReader = findDynamicLocaleReader(key, locale)
                .orElseGet(() -> findDynamicLocaleReader(key, this.locale).orElse(null));
        if (localeReader == null) {
            actor.error(key);
            return;
        }
        localeReader.error(actor, key, args);
    }

    @Override public @NotNull String get(@NotNull String key) {
        return get(key, locale);
    }

    @Override public @NotNull String get(@NotNull String key, @NotNull Locale locale) {
        notNull(key, "key");
        notNull(locale, "locale");
        return findLocaleMessage(key, locale)
                .orElseGet(() -> findLocaleMessage(key, this.locale).orElse(key));
    }

    @Override public void add(@NotNull DynamicLocaleReader<?> reader) {
        LinkedList<DynamicLocaleReader<?>> list = registeredBundles.computeIfAbsent(reader.getLocale(), v -> new LinkedList<>());
        list.push(reader);
    }

    @Override public @NotNull Locale getLocale() {
        return locale;
    }

    @Override public void setLocale(@NotNull Locale locale) {
        notNull(locale, "locale");
        this.locale = locale;
    }

    @Override
    public void addResourceBundle(@NotNull String resourceBundle, @NotNull Locale... locales) {
        notNull(resourceBundle, "resource bundle");
        notNull(locales, "locales");
        for (Locale locale : locales) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(resourceBundle, locale, getClass().getClassLoader(), UTF8Control.INSTANCE);
                add(bundle);
            } catch (MissingResourceException ignored) {
            }
        }
    }

    @Override
    public void addResourceBundle(@NotNull String resourceBundle) {
        notNull(resourceBundle, "resource bundle");
        for (Locale locale : Locales.getLocales()) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(resourceBundle, locale, getClass().getClassLoader(), UTF8Control.INSTANCE);
                add(bundle);
            } catch (MissingResourceException ignored) {
            }
        }
    }

    @Override
    public void add(@NotNull ResourceBundle resourceBundle) {
        notNull(resourceBundle, "resource bundle");
        add(LocaleReader.wrap(resourceBundle));
    }

    private Optional<String> findLocaleMessage(@NotNull String key, @NotNull Locale locale) {
        return findLocaleReader(key, locale).map(reader -> reader.get(key));
    }

    private Optional<LocaleReader> findLocaleReader(@NotNull String key, @NotNull Locale locale) {
        if (!registeredBundles.containsKey(locale))
            return Optional.empty();
        return registeredBundles.get(locale).stream()
                .filter(reader -> reader.containsKey(key))
                .filter(reader -> reader instanceof LocaleReader)
                .map(reader -> (LocaleReader) reader)
                .findFirst();
    }

    private Optional<DynamicLocaleReader<?>> findDynamicLocaleReader(@NotNull String key, @NotNull Locale locale) {
        if (!registeredBundles.containsKey(locale))
            return Optional.empty();
        return registeredBundles.get(locale).stream().filter(reader -> reader.containsKey(key)).findFirst();
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

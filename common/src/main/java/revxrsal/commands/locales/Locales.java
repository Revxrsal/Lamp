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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static revxrsal.commands.util.Preconditions.notNull;

/**
 * An enum-like class for packing all the available locales in Lamp.
 * <p>
 * Due to the fact that {@link Locale} does not define all locales,
 * additional ones are added.
 */
public final class Locales {

    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale GERMAN = Locale.GERMAN;
    public static final Locale FRENCH = Locale.FRENCH;
    public static final Locale JAPANESE = Locale.JAPANESE;
    public static final Locale ITALIAN = Locale.ITALIAN;
    public static final Locale KOREAN = Locale.KOREAN;
    public static final Locale CHINESE = Locale.CHINESE;
    public static final Locale SIMPLIFIED_CHINESE = Locale.SIMPLIFIED_CHINESE;
    public static final Locale TRADITIONAL_CHINESE = Locale.TRADITIONAL_CHINESE;
    public static final Locale SPANISH = new Locale("es");
    public static final Locale DUTCH = new Locale("nl");
    public static final Locale DANISH = new Locale("da");
    public static final Locale CZECH = new Locale("cs");
    public static final Locale GREEK = new Locale("el");
    public static final Locale LATIN = new Locale("la");
    public static final Locale BULGARIAN = new Locale("bg");
    public static final Locale AFRIKAANS = new Locale("af");
    public static final Locale HINDI = new Locale("hi");
    public static final Locale HEBREW = new Locale("he");
    public static final Locale POLISH = new Locale("pl");
    public static final Locale PORTUGUESE = new Locale("pt");
    public static final Locale FINNISH = new Locale("fi");
    public static final Locale SWEDISH = new Locale("sv");
    public static final Locale RUSSIAN = new Locale("ru");
    public static final Locale ROMANIAN = new Locale("ro");
    public static final Locale VIETNAMESE = new Locale("vi");
    public static final Locale THAI = new Locale("th");
    public static final Locale TURKISH = new Locale("tr");
    public static final Locale UKRANIAN = new Locale("uk");
    public static final Locale ARABIC = new Locale("ar");
    public static final Locale WELSH = new Locale("cy");
    public static final Locale NORWEGIAN_BOKMAAL = new Locale("nb");
    public static final Locale NORWEGIAN_NYNORSK = new Locale("nn");
    public static final Locale HUNGARIAN = new Locale("hu");

    /**
     * Returns the locale that matches the given language.
     * <p>
     * get("en") -> ENGLISH
     * <br>
     * get("fr") -> FRENCH
     *
     * @param language Language to get locale for
     * @return The locale, or null if not found.
     */
    public static Locale get(@NotNull String language) {
        notNull(language, "language");
        return LOCALES.get(language);
    }

    /**
     * Returns all the locales in this class
     *
     * @return All locales
     */
    public static Iterable<Locale> getLocales() {
        return LOCALES.values();
    }

    private Locales() {
        throw new AssertionError("You should not be attempting to instantiate this class.");
    }

    private static final Map<String, Locale> LOCALES;

    static {
        Map<String, Locale> locales = new HashMap<>();
        for (Field field : Locales.class.getDeclaredFields()) {
            if (field.getType() == Locale.class) {
                try {
                    Locale locale = (Locale) field.get(null);
                    locales.putIfAbsent(locale.getLanguage(), locale);
                    locales.put(locale.toString(), locale);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        LOCALES = Collections.unmodifiableMap(locales);
    }
}

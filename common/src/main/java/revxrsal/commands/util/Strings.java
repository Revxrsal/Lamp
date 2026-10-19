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
package revxrsal.commands.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.list.AnnotationList;

import java.util.*;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static revxrsal.commands.util.Preconditions.cannotInstantiate;

/**
 * General utilities for string operations
 */
public final class Strings {

    /**
     * Pattern to split by whitespace
     */
    public static final Pattern SPACE = Pattern.compile(" ", Pattern.LITERAL);
    /**
     * Pattern to extract snowflake IDs. Useful for JDA
     */
    public static final Pattern SNOWFLAKE = Pattern.compile("<(@!|@|@&|#)(?<snowflake>\\d{18})>");

    private Strings() {
        cannotInstantiate(Strings.class);
    }

    /**
     * Returns the candidates that the input may be a typo of, closest first. A
     * candidate matches if it starts with the input, or is within a few edits of it
     * (inserting, deleting or replacing a letter, or swapping two adjacent letters).
     * Case is ignored.
     *
     * @param input      The mistyped input
     * @param candidates The valid values
     * @param limit      The maximum number of matches to return
     * @return The closest matches
     */
    public static @NotNull List<String> closestMatches(@NotNull String input, @NotNull Collection<String> candidates, int limit) {
        if (input.isEmpty())
            return java.util.Collections.emptyList();
        String typed = input.toLowerCase(Locale.ROOT);
        // The same threshold Clang uses for its typo suggestions
        int maxDistance = (typed.length() + 2) / 3;
        Map<String, Integer> distances = new LinkedHashMap<>();
        for (String candidate : candidates) {
            String lower = candidate.toLowerCase(Locale.ROOT);
            int distance = lower.startsWith(typed) ? 0 : editDistance(typed, lower);
            if (distance <= maxDistance)
                distances.put(candidate, distance);
        }
        List<String> matches = new ArrayList<>(distances.keySet());
        matches.sort(Comparator.comparingInt(distances::get));
        return matches.subList(0, Math.min(limit, matches.size()));
    }

    /**
     * The optimal string alignment distance (Damerau–Levenshtein, where
     * no substring is edited twice)
     */
    private static int editDistance(String a, String b) {
        int[][] d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++)
            d[i][0] = i;
        for (int j = 0; j <= b.length(); j++)
            d[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1))
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
            }
        }
        return d[a.length()][b.length()];
    }

    public static @Nullable String getSnowflake(String mention) {
        Matcher matcher = SNOWFLAKE.matcher(mention);
        if (matcher.find())
            return matcher.group(2);
        return null;
    }

    public static Optional<String> getOverriddenName(@NotNull AnnotationList parameter) {
        Named named = parameter.get(Named.class);
        if (named != null) {
            return Optional.of(named.value());
        }
        return Optional.empty();
    }

    public static @NotNull String stripNamespace(String namespace, @NotNull String command) {
        int colon = command.indexOf(namespace + ':');
        if (colon == -1) {
            return command;
        }
        // +1 for the ':'
        return command.substring(namespace.length() + 1);
    }

    public static @NotNull String stripNamespace(@NotNull String command) {
        int colon = command.indexOf(':');
        if (colon == -1)
            return command;
        return command.substring(colon + 1);
    }

    public static @NotNull String removeRanges(
            @NotNull String input,
            @NotNull List<StringRange> ranges
    ) {
        ranges.sort(Comparator.comparingInt(StringRange::start));

        StringBuilder builder = new StringBuilder();
        int currentIndex = 0;  // Tracks the current index in the input string

        for (StringRange range : ranges) {
            if (currentIndex < range.start()) {
                builder.append(input, currentIndex, range.start());
            }
            currentIndex = range.end();
        }

        if (currentIndex < input.length()) {
            builder.append(input, currentIndex, input.length());
        }

        return builder.toString();
    }

    public static final class StringRange {
        private final int start;
        private final int end;

        public StringRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int start() {return start;}

        public int end() {return end;}

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            StringRange that = (StringRange) obj;
            return this.start == that.start &&
                    this.end == that.end;
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }

        @Override
        public String toString() {
            return "StringRange[" +
                    "start=" + start + ", " +
                    "end=" + end + ']';
        }
    }

}

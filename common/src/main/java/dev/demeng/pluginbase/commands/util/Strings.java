package dev.demeng.pluginbase.commands.util;

import static dev.demeng.pluginbase.commands.util.Preconditions.checkArgument;
import static dev.demeng.pluginbase.commands.util.Preconditions.notNull;

import dev.demeng.pluginbase.commands.annotation.Flag;
import dev.demeng.pluginbase.commands.annotation.Named;
import dev.demeng.pluginbase.commands.annotation.Switch;
import dev.demeng.pluginbase.text.Text;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * General utilities for string operations
 */
public final class Strings {

  private Strings() {
  }

  /**
   * Pattern to split by whitespace
   */
  public static final Pattern SPACE = Pattern.compile(" ", Pattern.LITERAL);

  /**
   * Pattern to split by whitespace
   */
  @SuppressWarnings("RegExpEmptyAlternationBranch") // we have LITERAL :face_palm:
  public static final Pattern VERTICAL_BAR = Pattern.compile("|", Pattern.LITERAL);

  /**
   * Pattern to extract snowflake IDs. Useful for JDA
   */
  public static final Pattern SNOWFLAKE = Pattern.compile("<(@!|@|@&|#)(?<snowflake>\\d{18})>");

  public static LinkedList<String> splitBySpace(String text) {
    String[] result = SPACE.split(text);
    LinkedList<String> list = new LinkedList<>();
    Collections.addAll(list, result);
    return list;
  }

  public static @Nullable String getSnowflake(String mention) {
    Matcher matcher = SNOWFLAKE.matcher(mention);
    if (matcher.find()) {
      return matcher.group(2);
    }
    return null;
  }

  public static String getName(@NotNull Parameter parameter) {
    Named named = parameter.getAnnotation(Named.class);
    if (named != null) {
      return named.value();
    }
    Switch switchAnn = parameter.getAnnotation(Switch.class);
    if (switchAnn != null) {
      return switchAnn.value().isEmpty() ? parameter.getName() : switchAnn.value();
    }
    Flag flag = parameter.getAnnotation(Flag.class);
    if (flag != null) {
      return flag.value().isEmpty() ? parameter.getName() : flag.value();
    }
    return parameter.getName();
  }

  public static String repeat(String string, int count) {
    notNull(string, "string");

    if (count <= 1) {
      checkArgument(count >= 0, "invalid count: " + count);
      return (count == 0) ? "" : string;
    }

    final int len = string.length();
    final long longSize = (long) len * (long) count;
    final int size = (int) longSize;
    if (size != longSize) {
      throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);
    }

    final char[] array = new char[size];
    string.getChars(0, len, array, 0);
    int n;
    for (n = len; n < size - n; n <<= 1) {
      System.arraycopy(array, 0, array, n, n);
    }
    System.arraycopy(array, 0, array, n, size - n);
    return new String(array);
  }

  public static String colorize(@NotNull String text) {
    return Text.colorize(text);
  }
}

package revxrsal.commands.util;

import java.util.Collection;
import java.util.Objects;

public final class Preconditions {

  private Preconditions() {
  }

  public static <T> void notEmpty(T[] array, String err) {
    if (array.length == 0) {
      throw new IllegalStateException(err);
    }
  }

  public static <T> void notEmpty(Collection<T> collection, String err) {
    if (collection.size() == 0) {
      throw new IllegalStateException(err);
    }
  }

  public static <T> void notEmpty(String s, String err) {
    if (s.length() == 0) {
      throw new IllegalStateException(err);
    }
  }

  public static void checkArgument(boolean expr, String err) {
    if (!expr) {
      throw new IllegalArgumentException(err);
    }
  }

  public static int coerceIn(int value, int min, int max) {
    return value < min ? min : Math.min(value, max);
  }

  public static int coerceAtMost(int value, int max) {
    return Math.min(value, max);
  }

  public static int coerceAtLeast(int value, int min) {
    return Math.max(value, min);
  }

  public static <T> T notNull(T t, String err) {
    return Objects.requireNonNull(t, err + " cannot be null!");
  }

}

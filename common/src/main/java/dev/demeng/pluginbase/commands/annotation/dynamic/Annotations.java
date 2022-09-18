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
package dev.demeng.pluginbase.commands.annotation.dynamic;

import dev.demeng.pluginbase.commands.util.Preconditions;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * A utility for constructing annotations dynamically.
 * <p>
 * Re-adapted from Guice.
 */
public final class Annotations {

  /**
   * Creates a new annotation with no values. Any default values will automatically be used.
   *
   * @param type The annotation type
   * @param <T>  Annotation type
   * @return The newly created annotation
   */
  public static @NotNull <T extends Annotation> T create(@NotNull Class<T> type) {
    return create(type, Collections.emptyMap());
  }

  /**
   * Creates a new annotation with the given map values. Any default values will automatically be
   * used if not specified in the map.
   * <p>
   * Note that the map may also use {@link Supplier}s instead of direct values.
   *
   * @param type    The annotation type
   * @param members The annotation members
   * @param <T>     Annotation type
   * @return The newly created annotation
   */
  public static @NotNull <T extends Annotation> T create(@NotNull Class<T> type,
      @NotNull Map<String, Object> members) {
    Preconditions.notNull(type, "type");
    Preconditions.notNull(members, "members");
    return type.cast(Proxy.newProxyInstance(
        type.getClassLoader(),
        new Class<?>[]{type},
        new DynamicAnnotationHandler(type, members)
    ));
  }

  /**
   * Creates a new annotation with the given map values. Any default values will automatically be
   * used if not specified in the map.
   * <p>
   * Note that the map may also use {@link Supplier}s instead of direct values.
   *
   * @param type The annotation type
   * @param <T>  Annotation type
   * @return The newly created annotation
   */
  public static @NotNull <T extends Annotation> T create(@NotNull Class<T> type,
      @NotNull Object... members) {
    Preconditions.notNull(type, "type");
    Preconditions.notNull(members, "members");
    if (members.length % 2 != 0) {
      throw new IllegalArgumentException(
          "Cannot have a non-even amount of members! Found " + members.length);
    }
    Map<String, Object> values = new HashMap<>();
    for (int i = 0; i < members.length; i += 2) {
      String key = String.valueOf(members[i]);
      Object value = members[i + 1];
      values.put(key, value);
    }
    return type.cast(Proxy.newProxyInstance(
        type.getClassLoader(),
        new Class<?>[]{type},
        new DynamicAnnotationHandler(type, values)
    ));
  }

  /**
   * Implementation of {@link Annotation#hashCode()}.
   *
   * @param type    The annotation type
   * @param members The annotation members
   * @return The annotation's hashcode.
   */
  private static int hashCode(Class<? extends Annotation> type, Map<String, Object> members) {
    int result = 0;
    for (Method method : type.getDeclaredMethods()) {
      String name = method.getName();
      Object value = members.get(name);
      result += (127 * name.hashCode()) ^ (Arrays.deepHashCode(new Object[]{value}) - 31);
    }
    return result;
  }

  /**
   * Implementation of {@link Annotation#equals(Object)}.
   *
   * @param type    The annotation type
   * @param members The annotation members
   * @param other   The other annotation to compare
   * @return if they are equal
   */
  private static boolean equals(Class<? extends Annotation> type, Map<String, Object> members,
      Object other) throws Exception {
    if (!type.isInstance(other)) {
      return false;
    }
    for (Method method : type.getDeclaredMethods()) {
      String name = method.getName();
      if (!Arrays.deepEquals(new Object[]{method.invoke(other)}, new Object[]{members.get(name)})) {
        return false;
      }
    }
    return true;
  }

  /**
   * Implementation of {@link Annotation#toString()}.
   *
   * @param type    The annotation type
   * @param members The annotation members
   * @return The annotation's hashcode.
   */
  private static String toString(Class<? extends Annotation> type, Map<String, Object> members) {
    StringBuilder sb = new StringBuilder().append("@").append(type.getName()).append("(");
    StringJoiner joiner = new StringJoiner(", ");
    for (Entry<String, Object> entry : members.entrySet()) {
      joiner.add(entry.getKey() + "=" + deepToString(entry.getValue()));
    }
    sb.append(joiner);
    return sb.append(")").toString();
  }

  private static String deepToString(Object arg) {
    String s = Arrays.deepToString(new Object[]{arg});
    return s.substring(1, s.length() - 1); // cut off the []
  }

  private static class DynamicAnnotationHandler implements InvocationHandler {

    private final Class<? extends Annotation> annotationType;
    private final Map<String, Object> annotationMembers;

    DynamicAnnotationHandler(Class<? extends Annotation> annotationType,
        Map<String, Object> annotationMembers) {
      this.annotationType = annotationType;
      this.annotationMembers = new HashMap<>(annotationMembers);
      for (Method method : annotationType.getDeclaredMethods()) {
        this.annotationMembers.putIfAbsent(method.getName(), method.getDefaultValue());
      }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      switch (method.getName()) {
        case "toString":
          return Annotations.toString(annotationType, annotationMembers);
        case "hashCode":
          return Annotations.hashCode(annotationType, annotationMembers);
        case "equals":
          return Annotations.equals(annotationType, annotationMembers, args[0]);
        case "annotationType":
          return annotationType;
        default: {
          Object v = annotationMembers.get(method.getName());
          if (v == null) {
            throw new AbstractMethodError(method.getName());
          }
          return v instanceof Supplier ? ((Supplier<?>) v).get() : v;
        }
      }
    }
  }
}


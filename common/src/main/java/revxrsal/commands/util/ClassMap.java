package revxrsal.commands.util;

import java.util.HashMap;
import org.jetbrains.annotations.NotNull;

public final class ClassMap<V> extends HashMap<Class<?>, V> {

  public boolean add(Class<?> type, V value) {
    Class<?> wrapped = Primitives.wrap(type);
    if (containsKey(wrapped)) {
      return false;
    }
    put(wrapped, value);
    return false;
  }

  public V getFlexibleOrDefault(@NotNull Class<?> key, V def) {
    V value = getFlexible(key);
    if (value == null) {
      return def;
    }
    return value;
  }

  public V getFlexible(@NotNull Class<?> key) {
    key = Primitives.wrap(key);
    V v = get(key);
    if (v != null) {
      return v;
    }
    for (Entry<Class<?>, V> entry : entrySet()) {
      if (entry.getKey().isAssignableFrom(key)) {
        v = entry.getValue();
        break;
      }
    }
    put(key, v);
    return v;
  }
}

package dev.demeng.pluginbase.commands.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public final class Collections {

  private Collections() {
  }

  public static <T> LinkedList<T> linkedListOf(T... elements) {
    LinkedList<T> list = new LinkedList<>();
    java.util.Collections.addAll(list, elements);
    return list;
  }

  public static <T> List<T> listOf(T... elements) {
    List<T> list = new ArrayList<>();
    java.util.Collections.addAll(list, elements);
    return list;
  }

  public static <T> @Nullable T getOrNull(List<T> list, int index) {
    return index >= 0 && index <= (list.size() - 1) ? list.get(index) : null;
  }

}

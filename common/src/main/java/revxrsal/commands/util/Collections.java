package revxrsal.commands.util;

import java.util.*;

@SuppressWarnings("unchecked")
public final class Collections {

    private Collections() {}

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

}

package revxrsal.commands.util;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.addAll;
import static revxrsal.commands.util.Collections.listOf;

public final class Reflections {

    private Reflections() {
    }

    /**
     * Finds all {@link Method}s defined by a class, including private ones
     * and ones that are inherited from classes.
     *
     * @param c Class to get for
     * @return A list of all methods
     */
    public static Set<Method> getAllMethods(Class<?> c) {
        Set<Method> methods = new HashSet<>();
        Class<?> current = c;
        while (current != null && current != Object.class) {
            addAll(methods, current.getDeclaredMethods());
            current = current.getSuperclass();
        }
        return methods;
    }

    /**
     * Returns a hierarchy of classes that are contained by the given class. The
     * order of the list matters, as it starts from the parent and ends at the child
     *
     * @param c Class to find for
     * @return A list of all classes containing each other
     */
    public static List<Class<?>> getTopClasses(Class<?> c) {
        List<Class<?>> classes = listOf(c);
        Class<?> enclosingClass = c.getEnclosingClass();
        while (c.getEnclosingClass() != null) {
            classes.add(c = enclosingClass);
        }
        java.util.Collections.reverse(classes);
        return classes;
    }
}

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
package revxrsal.commands.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.addAll;
import static revxrsal.commands.util.Preconditions.cannotInstantiate;

public final class Reflections {

    private Reflections() {
        cannotInstantiate(Reflections.class);
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
        List<Class<?>> classes = new ArrayList<>();
        classes.add(c);
        Class<?> enclosingClass = c.getEnclosingClass();
        while (c.getEnclosingClass() != null) {
            classes.add(c = enclosingClass);
        }
        java.util.Collections.reverse(classes);
        return classes;
    }
}

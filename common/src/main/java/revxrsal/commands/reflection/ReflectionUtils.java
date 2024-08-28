package revxrsal.commands.reflection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class ReflectionUtils {
    /**
     * Get the package name of a given class
     *
     * @param clazz
     * @return The package name of the given class
     */
    public String getPackageName(Class<?> clazz) {
        return clazz.getPackage().getName();
    }

    /**
     * Find all classes and subclasses
     *
     * @param directory
     * @param packageName
     * @param classes
     */
    private void findClasses(File directory, String packageName, Set<Class<?>> classes){
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                String subPackageName = packageName + "." + file.getName();
                findClasses(file, subPackageName, classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Get a set of all project's classes
     *
     * @param packageName
     * @return All classes contained in a given package
     */
    public Set<Class<?>> getClasses(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = null;
        try {
            resources = classLoader.getResources(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File directory = new File(resource.getFile());
            if (directory.exists()) {
                findClasses(directory, packageName, classes);
            }
        }

        return classes;
    }

    /**
     * Get the class from which the project is started
     *
     * @return The class from which the project is started.
     */
    public static Class<?> getMainClass() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        try {
            return Class.forName(stackTrace[stackTrace.length - 1].getClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }
}

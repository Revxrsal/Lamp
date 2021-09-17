package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public final class AnnotationReader implements Iterable<Annotation> {

    private static final List<Class<? extends Annotation>> ANNOTATIONS = Arrays.asList(
            Subcommand.class,
            Command.class,
            Default.class
    );

    private final Map<Class<?>, Annotation> annotations = new HashMap<>();
    private boolean empty = false;

    public AnnotationReader(AnnotatedElement element) {
        for (Annotation annotation : element.getDeclaredAnnotations()) {
            annotations.put(annotation.annotationType(), annotation);
        }
    }

    public AnnotationReader(Method element) {
        Annotation[] method = element.getDeclaredAnnotations();
        boolean ignore = true;
        for (Annotation annotation : method) {
            if (ANNOTATIONS.contains(annotation.annotationType())) {
                ignore = false;
                break;
            }
        }
        if (ignore) {
            empty = true;
            return;
        }
        for (Annotation annotation : element.getDeclaringClass().getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(DistributeOnMethods.class)) {
                annotations.put(annotation.annotationType(), annotation);
            }
        }

        // method annotations receive higher priority.
        for (Annotation annotation : method) {
            annotations.put(annotation.annotationType(), annotation);
        }
    }

    public boolean contains(Class<? extends Annotation> annotation) {
        return annotations.containsKey(annotation);
    }

    public boolean isEmpty() {
        return empty;
    }

    public <T extends Annotation> T get(@NotNull Class<T> type) {
        return (T) annotations.get(type);
    }

    public <R, T extends Annotation> R get(@NotNull Class<T> type, Function<T, R> f) {
        return get(type, f, () -> null);
    }

    public <R, T extends Annotation> R get(@NotNull Class<T> type, Function<T, R> f, Supplier<R> def) {
        T ann = (T) annotations.get(type);
        if (ann != null)
            return f.apply(ann);
        return def.get();
    }

    public <T extends Annotation> @NotNull T get(@NotNull Class<T> type, String err) {
        T ann = get(type);
        if (ann == null)
            throw new IllegalStateException(err);
        return ann;
    }

    @SafeVarargs public final boolean hasAll(Class<? extends Annotation>... types) {
        for (Class<? extends Annotation> type : types) {
            if (!annotations.containsKey(type))
                return false;
        }
        return true;
    }

    @NotNull @Override public Iterator<Annotation> iterator() {
        return annotations.values().iterator();
    }
}

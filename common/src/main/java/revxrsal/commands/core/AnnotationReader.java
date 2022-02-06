package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.DistributeOnMethods;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.OrphanRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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

    public AnnotationReader(Parameter parameter) {
        for (Annotation annotation : parameter.getDeclaredAnnotations()) {
            annotations.put(annotation.annotationType(), annotation);
        }
    }

    private static Command createCommand(List<CommandPath> paths) {
        String[] pathsArray = paths.stream().map(CommandPath::toRealString).toArray(String[]::new);
        return new Command() {
            @Override public Class<? extends Annotation> annotationType() {return Command.class;}

            @Override public String[] value() {return pathsArray;}
        };
    }

    public AnnotationReader(Class<?> container, Method element, @NotNull Object target) {
        Annotation[] method = element.getDeclaredAnnotations();
        boolean ignore = true;

        // methods that have none of our command annotations should
        // be discarded and ignored
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

        if (OrphanCommand.class.isAssignableFrom(container)) { // do it first since it's the lowest priority.
            annotations.put(Command.class, createCommand(((OrphanRegistry) target).getParentPaths()));
        }

        for (Annotation annotation : element.getDeclaringClass().getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(DistributeOnMethods.class)) {
                annotations.put(annotation.annotationType(), annotation);
            }
        }
        for (Annotation annotation : container.getDeclaredAnnotations()) {
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

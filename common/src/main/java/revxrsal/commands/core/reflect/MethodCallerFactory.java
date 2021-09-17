package revxrsal.commands.core.reflect;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * Factory for creating {@link MethodCaller}s for methods.
 */
public interface MethodCallerFactory {

    /**
     * Creates a new {@link MethodCaller} for the specified method.
     *
     * @param method Method to create for
     * @return The reflective method caller
     * @throws Throwable Any exceptions during creation
     */
    @NotNull MethodCaller createFor(@NotNull Method method) throws Throwable;

    /**
     * Returns the default {@link MethodCallerFactory}, which uses
     * the method handles API to create method callers.
     *
     * @return The default method caller factory.
     */
    static MethodCallerFactory defaultFactory() {
        return MethodHandlesCallerFactory.INSTANCE;
    }

}

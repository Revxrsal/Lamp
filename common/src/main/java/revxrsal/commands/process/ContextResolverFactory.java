package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandParameter;

public interface ContextResolverFactory {

    /**
     * Creates a context resolver for the specified type, or {@code null} if this type
     * is not supported by this factory.
     *
     * @param parameter The parameter to create for
     * @return The {@link ValueResolver}, or null if not supported.
     */
    @Nullable ContextResolver<?> create(@NotNull CommandParameter parameter);

    /**
     * Creates a {@link ContextResolverFactory} that will return the same
     * resolver for all parameters that match a specific type
     *
     * @param type     Type to check for
     * @param resolver The value resolver to use
     * @param <T>      The resolver value type
     * @return The resolver factory
     */
    static <T> @NotNull ContextResolverFactory forType(Class<T> type, ContextResolver<T> resolver) {
        return parameter -> parameter.getType() == type ? resolver : null;
    }

    /**
     * Creates a {@link ContextResolverFactory} that will return the same
     * resolver for all parameters that match or extend a specific type
     *
     * @param type     Type to check for
     * @param resolver The value resolver to use
     * @param <T>      The resolver value type
     * @return The resolver factory
     */
    static <T> @NotNull ContextResolverFactory forHierarchyType(Class<T> type, ContextResolver<T> resolver) {
        return parameter -> parameter.getType() == type || parameter.getType().isAssignableFrom(type) ? resolver : null;
    }
}

package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandParameter;

/**
 * Creates a {@link ContextResolver} for specific types of parameters. These are most useful in the
 * following cases:
 * <ul>
 *     <li>Creating a context resolver for only a specific type of parameters,
 *     for example those with a specific annotation</li>
 *     <li>Creating context resolvers for a common interface or class</li>
 * </ul>
 * <p>
 * Example: We want to register a special context resolver for org.bukkit.Locations that
 * are annotated with a specific annotation, in which the argument will be fed with the player's
 * target location
 * <pre>{@code
 * @Target(ElementType.PARAMETER)
 * @Retention(RetentionPolicy.RUNTIME)
 * public @interface LookingLocation {
 *
 * }
 *
 * public final class LookingLocationFactory implements ContextResolverFactory {
 *
 *     @Override public @Nullable ContextResolver<?> create(@NotNull CommandParameter parameter) {
 *         if (parameter.getType() != Location.class) return null;
 *         if (!parameter.hasAnnotation(LookingLocation.class)) return null;
 *         return (actor, p, command) -> {
 *             Player player = ((BukkitCommandActor) actor).requirePlayer();
 *             return player.getTargetBlock(null, 200).getLocation();
 *         };
 *     }
 * }
 * }</pre>
 * <p>
 * Note that {@link ContextResolverFactory}ies must be registered
 * with {@link CommandHandler#registerContextResolverFactory(ContextResolverFactory)}.
 */
public interface ContextResolverFactory {

  /**
   * Creates a context resolver for the specified type, or {@code null} if this type is not
   * supported by this factory.
   *
   * @param parameter The parameter to create for
   * @return The {@link ValueResolver}, or null if not supported.
   */
  @Nullable ContextResolver<?> create(@NotNull CommandParameter parameter);

  /**
   * Creates a {@link ContextResolverFactory} that will return the same resolver for all parameters
   * that match a specific type
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
   * Creates a {@link ContextResolverFactory} that will return the same resolver for all parameters
   * that match or extend a specific type
   *
   * @param type     Type to check for
   * @param resolver The value resolver to use
   * @param <T>      The resolver value type
   * @return The resolver factory
   */
  static <T> @NotNull ContextResolverFactory forHierarchyType(Class<T> type,
      ContextResolver<T> resolver) {
    return parameter -> parameter.getType() == type || parameter.getType().isAssignableFrom(type)
        ? resolver : null;
  }
}

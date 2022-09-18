package dev.demeng.pluginbase.commands.process;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.annotation.CaseSensitive;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Creates a {@link ValueResolver} for specific types of parameters. These are most useful in the
 * following cases:
 * <ul>
 *     <li>Creating a value resolver for only a specific type of parameters,
 *     for example those with a specific annotation</li>
 *     <li>Creating value resolvers for a common interface or class</li>
 * </ul>
 * <p>
 * For example, the following is a resolver factory that finds values for enums,
 * while respecting {@link CaseSensitive} parameters.
 * <pre>{@code
 * enum EnumResolverFactory implements ValueResolverFactory {
 *
 *     INSTANCE;
 *
 *     @Override public @Nullable ValueResolver<?> create(@NotNull CommandParameter parameter) {
 *         Class<?> type = parameter.getType();
 *         if (!type.isEnum()) return null;
 *         Class<? extends Enum> enumType = type.asSubclass(Enum.class);
 *         Map<String, Enum<?>> values = new HashMap<>();
 *         boolean caseSensitive = parameter.hasAnnotation(CaseSensitive.class);
 *         for (Enum<?> enumConstant : enumType.getEnumConstants()) {
 *             if (caseSensitive)
 *                 values.put(enumConstant.name(), enumConstant);
 *             else
 *                 values.put(enumConstant.name().toLowerCase(), enumConstant);
 *         }
 *         return (ValueResolver<Enum<?>>) (arguments, actor, parameter1, command) -> {
 *             String value = arguments.pop();
 *             Enum<?> v = values.get(caseSensitive ? value : value.toLowerCase());
 *             if (v == null)
 *                 throw new EnumNotFoundException(parameter, value, actor);
 *             return v;
 *         };
 *     }
 * }}</pre>
 * <p>
 * Note that {@link ValueResolverFactory}ies must be registered
 * with {@link CommandHandler#registerValueResolverFactory(ValueResolverFactory)}.
 */
public interface ValueResolverFactory {

  /**
   * Creates a value resolver for the specified type, or {@code null} if this type is not supported
   * by this factory.
   *
   * @param parameter The parameter to create for
   * @return The {@link ValueResolver}, or null if not supported.
   */
  @Nullable ValueResolver<?> create(@NotNull CommandParameter parameter);

  /**
   * Creates a {@link ValueResolverFactory} that will return the same resolver for all parameters
   * that match a specific type
   *
   * @param type     Type to check for
   * @param resolver The value resolver to use
   * @param <T>      The resolver value type
   * @return The resolver factory
   */
  static <T> @NotNull ValueResolverFactory forType(Class<T> type, ValueResolver<T> resolver) {
    return (parameter) -> parameter.getType() == type ? resolver : null;
  }

  /**
   * Creates a {@link ValueResolverFactory} that will return the same resolver for all parameters
   * that match or extend a specific type
   *
   * @param type     Type to check for
   * @param resolver The value resolver to use
   * @param <T>      The resolver value type
   * @return The resolver factory
   */
  static <T> @NotNull ValueResolverFactory forHierarchyType(Class<T> type,
      ValueResolver<T> resolver) {
    return (parameter) -> parameter.getType() == type || parameter.getType().isAssignableFrom(type)
        ? resolver : null;
  }
}

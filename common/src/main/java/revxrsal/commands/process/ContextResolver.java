package revxrsal.commands.process;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.process.ParameterResolver.ParameterResolverContext;

/**
 * A resolver for resolving values that are, by default, resolvable through the command invocation
 * context, and do not need any data from the arguments to find the value. An example context
 * resolver is finding the sender's world.
 *
 * @param <T> The resolved type
 */
public interface ContextResolver<T> {

  /**
   * Resolves the value of this resolver
   *
   * @param context The command resolving context.
   * @return The resolved value. May or may not be null.
   */
  T resolve(@NotNull ContextResolverContext context) throws Throwable;

  /**
   * Returns a context resolver that returns a static value. This is a simpler way for adding
   * constant values without having to deal with lambdas.
   *
   * @param value The value to return
   * @param <T>   The value type
   * @return The context resolver
   * @since 1.3.0
   */
  static <T> ContextResolver<T> of(@NotNull T value) {
    return context -> value;
  }

  /**
   * Returns a context resolver that returns a supplier value. This is a simpler way for adding
   * values without having to deal with lambdas.
   *
   * @param value The value supplier
   * @param <T>   The value type
   * @return The context resolver
   * @since 1.3.0
   */
  static <T> ContextResolver<T> of(@NotNull Supplier<T> value) {
    return context -> value.get();
  }

  /**
   * Represents the resolving context of {@link ContextResolver}. This contains all the relevant
   * information about the resolving context.
   */
  interface ContextResolverContext extends ParameterResolverContext {

  }

}

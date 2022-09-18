package dev.demeng.pluginbase.commands.core.reflect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A high-level wrapper, responsible for invoking methods reflectively.
 */
public interface MethodCaller {

  /**
   * Calls the method of this caller
   *
   * @param instance  Instance to call from. Can be null
   * @param arguments Invoking arguments
   * @return The return result
   */
  Object call(@Nullable Object instance, Object... arguments);

  /**
   * Binds this caller to the specified instance. Calls from the bound method caller will no longer
   * need an instance to call from.
   *
   * @param instance Instance to invoke from. Can be null in case of static methods.
   * @return The bound method caller
   */
  default BoundMethodCaller bindTo(@Nullable Object instance) {
    return arguments -> call(instance, arguments);
  }

  /**
   * Represents a {@link MethodCaller} that is attached to an instance
   */
  interface BoundMethodCaller {

    /**
     * Calls the method of this caller
     *
     * @param arguments Invoking arguments
     * @return The return result
     */
    Object call(@NotNull Object... arguments);
  }
}

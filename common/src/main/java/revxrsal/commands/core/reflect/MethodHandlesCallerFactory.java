package revxrsal.commands.core.reflect;

import static java.util.Collections.addAll;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link MethodCallerFactory} that uses the method handles API to generate method callers
 */
final class MethodHandlesCallerFactory implements MethodCallerFactory {

  public static final MethodHandlesCallerFactory INSTANCE = new MethodHandlesCallerFactory();

  @Override
  public @NotNull MethodCaller createFor(@NotNull Method method) throws Throwable {
    if (!method.isAccessible()) {
      method.setAccessible(true);
    }
    MethodHandle handle = MethodHandles.lookup().unreflect(method);
    String methodString = method.toString();
    boolean isStatic = Modifier.isStatic(method.getModifiers());
    return new MethodCaller() {
      @SneakyThrows
      @Override
      public Object call(@Nullable Object instance, Object... arguments) {
        if (!isStatic) {
          List<Object> args = new ArrayList<>();
          args.add(instance);
          addAll(args, arguments);
          return handle.invokeWithArguments(args);
        }
        return handle.invokeWithArguments(arguments);
      }

      @Override
      public String toString() {
        return "MethodHandlesCaller(" + methodString + ")";
      }
    };
  }

  @Override
  public String toString() {
    return "MethodHandlesCallerFactory";
  }
}

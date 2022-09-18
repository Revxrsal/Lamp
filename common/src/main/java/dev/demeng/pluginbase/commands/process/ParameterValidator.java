package dev.demeng.pluginbase.commands.process;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.exception.CommandExceptionHandler;
import org.jetbrains.annotations.NotNull;

/**
 * A validator for a specific parameter type. These validators can do extra checks on parameters
 * after they are resolved from {@link ValueResolver} or {@link ContextResolver}s.
 * <p>
 * Validators work on subclasses as well. For example, we can write a validator to validate a
 * custom
 * <code>@Range(min, max)</code> annotation for numbers:
 *
 * <pre>{@code
 * public enum RangeValidator implements ParameterValidator<Number> {
 *     INSTANCE;
 *
 *     @Override public void validate(Number value, @NotNull CommandParameter parameter, @NotNull CommandActor actor) throws Throwable {
 *         Range range = parameter.getAnnotation(Range.class);
 *         if (range == null) return;
 *         double d = value.doubleValue();
 *         if (d < range.min())
 *             throw new CommandErrorException(actor, "Specified value (" + d + ") is less than minimum " + range.min());
 *         if (d > range.max())
 *             throw new CommandErrorException(actor, "Specified value (" + d + ") is greater than maximum " + range.max());
 *     }
 * }
 * }</pre>
 * <p>
 * These can be registered through
 * {@link CommandHandler#registerParameterValidator(Class, ParameterValidator)}
 *
 * @param <T> The parameter handler
 */
public interface ParameterValidator<T> {

  /**
   * Validates the specified value that was passed to a parameter.
   * <p>
   * Ideally, a validator will want to throw an exception when the parameter is not valid, and then
   * further handled with {@link CommandExceptionHandler}.
   *
   * @param value     The parameter value. May or may not be null, depending on the resolver.
   * @param parameter The parameter that will take this value
   * @param actor     The command actor
   */
  void validate(T value, @NotNull CommandParameter parameter, @NotNull CommandActor actor);

}

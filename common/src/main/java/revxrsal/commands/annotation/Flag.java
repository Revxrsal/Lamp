package revxrsal.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark a parameter as a flag.
 * <p>
 * Flags are similar to normal parameters, however they do not need to come in a specific order, and
 * are explicitly named with a special prefix when the command is invoked.
 * <p>
 * For example, <code>/test (parameters) -name "hello there"</code>, in which <em>name</em> would be
 * a flag parameter.
 * <p>
 * Flags are compatible with {@link Default} and {@link Optional}, as in, they can be marked as
 * optional or can have a default value when not specified.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Flag {

  /**
   * The flag name. If left empty, the parameter name will be used.
   *
   * @return The flag name
   */
  String value() default "";

}

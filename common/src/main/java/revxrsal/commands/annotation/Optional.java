package revxrsal.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter as optional and does not need to be explicitly specified by
 * the sender.
 * <p>
 * Note that if the parameter has {@link Default} on it, it will automatically be marked
 * as optional.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Optional {}

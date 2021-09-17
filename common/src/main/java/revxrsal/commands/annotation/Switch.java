package revxrsal.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a (strictly) {@code boolean} parameter as a "switch", whose value will be
 * set by "flags", such as "-silent", which will represent a boolean parameter
 * annotated with {@code @Switch("silent")}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Switch {

    /**
     * The switch / flag name. If left empty, the parameter name will be used.
     *
     * @return The switch name
     */
    String value() default "";

    /**
     * Returns the default value of this switch
     *
     * @return The default switch value.
     */
    boolean defaultValue() default false;

}

package revxrsal.commands.annotation;

import revxrsal.commands.command.CommandParameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds a default value for a parameter when it is not supplied.
 * <p>
 * Due to limitations and simply the lots of "edge cases", this parameter only works if
 * there are no parameters after it, or if all following parameters are also
 * marked with {@link Default}.
 * <p>
 * Note that if any parameter is annotated with {@link Default}, it will
 * automatically be marked as optional.
 * <p>
 * Accessible with {@link CommandParameter#getDefaultValue()}.
 * <p>
 * Aside from being used on parameters, this annotation can also be used on methods
 * that should be invoked when an invalid command is inputted in the command
 * tree. Methods annotated with {@link Default} can have parameters just like any
 * other command methods.
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Default {

    /**
     * The default value. This will be passed to resolvers just as if the user inputted it.
     * <p>
     * If none is specified, and the argument is not present, {@code null}
     * will be passed to the command.
     *
     * @return The parameter default value.
     */
    String value() default "";

}

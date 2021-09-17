package revxrsal.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to override the names of command parameters.
 * <p>
 * When present, {@link #value()} will be used, otherwise, the compiler-stored name will
 * be used. Note that if the compiler does not reserve names, it will be "arg0", "arg1", etc.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Named {

    /**
     * The overridden parameter name
     *
     * @return The parameter name
     */
    String value();

}

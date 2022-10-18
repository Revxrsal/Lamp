package revxrsal.commands.annotation;

import revxrsal.commands.CommandHandler;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/**
 * An annotation for fields and parameters used to inject dependencies into command classes.
 *
 * @see CommandHandler#registerDependency(Class, Object)
 * @see CommandHandler#registerDependency(Class, Supplier)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Dependency {

}

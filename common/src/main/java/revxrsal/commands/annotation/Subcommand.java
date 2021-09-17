package revxrsal.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a subcommand that has a parent. Subcommand methods
 * <strong>must</strong> have a {@link Command} annotation, either on the method
 * itself, or on the declaring class.
 * <p>
 * Subcommand annotations are also replaceable by {@link Command} ones, however
 * when {@link Command} is used, it should include the entire command path.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Subcommand {

    /**
     * The subcommand name, and aliases if any. Values can contain spaces,
     * in which case it would automatically walk through the categories and
     * correctly calculate the command path.
     *
     * @return The subcommand name and aliases
     */
    String[] value();

}

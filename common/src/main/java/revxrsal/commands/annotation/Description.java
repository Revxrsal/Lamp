package revxrsal.commands.annotation;

import revxrsal.commands.command.ExecutableCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to give the {@link ExecutableCommand} a description.
 * <p>
 * Accessible with {@link ExecutableCommand#getDescription()}.
 */
@DistributeOnMethods
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {

    /**
     * The command description
     *
     * @return The description
     */
    String value();

}

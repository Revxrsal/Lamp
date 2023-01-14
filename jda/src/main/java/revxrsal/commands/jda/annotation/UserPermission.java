package revxrsal.commands.jda.annotation;

import revxrsal.commands.annotation.DistributeOnMethods;
import revxrsal.commands.annotation.NotSender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a command as being executable only by a certain user or
 * list of users.
 * <p>
 * Users may be specified in names ({@link #names()}), or IDs ({@link #ids()})
 */
@DistributeOnMethods
@NotSender.ImpliesNotSender
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserPermission {

    /**
     * The names of users who are allowed to execute the command
     *
     * @return The names
     */
    String[] names() default {};

    /**
     * The IDs of users who are allowed to execute the command
     *
     * @return The IDs
     */
    long[] ids() default {};

}

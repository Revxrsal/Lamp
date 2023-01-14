package revxrsal.commands.jda.annotation;

import revxrsal.commands.annotation.DistributeOnMethods;
import revxrsal.commands.annotation.NotSender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires one of certain roles to be present on the user executing
 * the command.
 * <p>
 * The user will be able to execute the command if they have <strong>at least</strong>
 * one of the specified roles.
 * <p>
 * Roles may be given in IDs ({@link #ids()}), or in names ({@link #names()})..
 */
@DistributeOnMethods
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@NotSender.ImpliesNotSender
public @interface RolePermission {

    /**
     * The names of roles the user must have at least one from.
     *
     * @return The role names
     */
    String[] names() default {};

    /**
     * The IDs of roles the user must have at least one from.
     *
     * @return The role IDs
     */
    long[] ids() default {};

}

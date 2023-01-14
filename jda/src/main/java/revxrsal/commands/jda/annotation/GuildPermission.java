package revxrsal.commands.jda.annotation;

import net.dv8tion.jda.api.Permission;
import revxrsal.commands.annotation.DistributeOnMethods;
import revxrsal.commands.annotation.NotSender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a guild permission, where a user is required to have
 * all the specified {@link Permission}s
 */
@DistributeOnMethods
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@NotSender.ImpliesNotSender
public @interface GuildPermission {

    /**
     * The permissions the user is required to have. Note that
     * they must have ALL the permissions to be able to execute the command.
     */
    Permission[] value();

}

package revxrsal.commands.jda.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import revxrsal.commands.annotation.DistributeOnMethods;
import revxrsal.commands.annotation.NotSender;

/**
 * Marks a command as NSFW. Used for slash commands.
 */
@DistributeOnMethods
@NotSender.ImpliesNotSender
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NSFW {
}

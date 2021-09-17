package revxrsal.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Adds a cooldown for the command.
 */
@DistributeOnMethods
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Cooldown {

    /**
     * The cooldown value
     *
     * @return The command cooldown value
     */
    long value();

    /**
     * The time unit in which the cooldown goes for.
     *
     * @return The time unit for the cooldown
     */
    TimeUnit unit() default TimeUnit.SECONDS;

}

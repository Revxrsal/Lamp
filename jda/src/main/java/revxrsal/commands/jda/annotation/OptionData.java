package revxrsal.commands.jda.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import revxrsal.commands.annotation.NotSender;

/**
 * Define parameter as custom {@link net.dv8tion.jda.api.interactions.commands.build.OptionData} instead of auto resolving.
 */
@NotSender.ImpliesNotSender
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionData {
    OptionType value();

    OptionChoice[] choices() default {};

    String name() default "";

    String description() default "";

    boolean required() default true;

    /**
     * Defines how {@link #choices()} will be resolved. If {@code true}, it should be proceeded by
     */
    boolean autocomplete() default false;
}

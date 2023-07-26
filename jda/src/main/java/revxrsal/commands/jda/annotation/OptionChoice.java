package revxrsal.commands.jda.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * Represents choices of {@link OptionType}. If {@link OptionType} is {@code OptionType.NUMBER, OptionType.INTEGER}, it will be automatically 'parsed' using
 * {@link Double#parseDouble(String)} or {@link Long#parseLong(String)}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionChoice {
    String name();

    String value();
}

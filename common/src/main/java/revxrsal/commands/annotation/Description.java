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
     * The command description. It is possible to use localized strings
     * inside this value, using the following syntax:
     * {@code #{message-key} }, which will effectively use values
     * from {@link revxrsal.commands.locales.Translator#get(String) Translator#get(key)}.
     *
     * @return The description
     */
    String value();

}

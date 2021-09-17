package revxrsal.commands.annotation;

import revxrsal.commands.command.CommandParameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to mark that a parameter should NOT be concatenated with the rest
 * of the command arguments.
 * <p>
 * Also used for {@link CommandParameter#consumesAllString()}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Single {}

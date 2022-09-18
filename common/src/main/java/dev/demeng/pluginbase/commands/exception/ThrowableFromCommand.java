package dev.demeng.pluginbase.commands.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an exception class as throwable from inside the command. Any exceptions annotated with this
 * annotation will not be wrapped by a {@link CommandInvocationException}.
 *
 * @see CommandInvocationException
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThrowableFromCommand {

}

package revxrsal.commands.exception;

import java.lang.annotation.*;

/**
 * Marks an exception class as throwable from inside the command. Any exceptions
 * annotated with this annotation will be unwrapped from {@link CommandInvocationException}. *
 *
 * @see CommandInvocationException
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThrowableFromCommand {}

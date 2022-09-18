package dev.demeng.pluginbase.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter of enum type that the value is case-sensitive.
 * <p>
 * By default, all enum types are case-insensitive.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CaseSensitive {

}

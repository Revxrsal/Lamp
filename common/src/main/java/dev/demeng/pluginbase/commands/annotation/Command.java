package dev.demeng.pluginbase.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The main entrypoint to a command, which specifies the parent of any command.
 * <p>
 * This can be used alone on methods, or can be added on a class to automatically mark all methods
 * inside the class as children of the given command.
 */
@DistributeOnMethods
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Command {

  /**
   * The command main name, and aliases if any. Values can contain spaces, in which case it would
   * automatically walk through the categories and correctly calculate the command path.
   *
   * @return The command name and aliases
   */
  String[] value();

}

package dev.demeng.pluginbase.commands.annotation;

import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to give the {@link ExecutableCommand} a usage.
 * <p>
 * If not present, it will be auto-generated
 * <p>
 * Accessible with {@link ExecutableCommand#getUsage()}
 */
@DistributeOnMethods
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Usage {

  /**
   * The command usage
   *
   * @return The usage
   */
  String value();

}

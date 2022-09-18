package dev.demeng.pluginbase.commands.annotation;

import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a command as a "secret" command. This can be used to prevent certain features such as
 * appearing in help menus, auto-completion, etc.
 * <p>
 * Accessible with {@link ExecutableCommand#isSecret()}
 */
@DistributeOnMethods
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecretCommand {

}

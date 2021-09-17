package revxrsal.commands.annotation;

import revxrsal.commands.command.ExecutableCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark command to be executed asynchronously.
 * <p>
 * Accessible though {@link ExecutableCommand#isAsync()}.
 * <p>
 * Note that no thread-safety cautions will be taken when executed! You must make sure
 * you are handling synchronisity and other thread-safety concepts appropriately.
 * <p>
 * Note that if a command returns a {@link java.util.concurrent.Future} or a subtype of it,
 * or {@link java.util.concurrent.CompletionStage} or a subtype of it, it will automatically be marked as
 * asynchronous.
 */
@DistributeOnMethods
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RunAsync {}

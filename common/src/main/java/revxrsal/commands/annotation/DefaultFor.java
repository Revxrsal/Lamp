package revxrsal.commands.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a command method as the default action to be taken when an invalid
 * command is inputted.
 * <p>
 * This example shows a common use case for this annotation:
 * <pre>{@code
 * @Command("test")
 * public class TestCommand {
 *
 *     @DefaultFor("test") // <--- Becomes the default action when running '/test [page]'
 *     @Subcommand("help") // <--- Also executes in '/test help [page]'
 *     public void help(@Optional(def = "1") int page) {
 *         ...
 *     }
 * }}
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultFor {

    /**
     * The paths to become the default for.
     * <p>
     * Note that, unlike {@link Command} and {@link Subcommand}, this path is absolute,
     * and is not relative to the parent command or method. This means the full path should be
     * inputted, and paths declared in the containing class or method will be ignored.
     *
     * @return The command paths which this method will become the default
     * action for.
     */
    @NotNull String[] value();

}

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
 * @Command({"test", "testing"})
 * public class TestCommand {
 *
 *     @DefaultFor("test") // <--- Becomes the default action for '/test [page]' AND '/tester [page]'
 *     @Subcommand("help") // <--- Also adds '/test help [page]'
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
     * The paths to become the default for. Note that this path is absolute,
     * it is not relative to the parent command or method. This means that only the command and not its aliases need to be inputted.
     *
     * @return The command paths which this method will become the default
     * action for.
     */
    @NotNull String[] value();

}

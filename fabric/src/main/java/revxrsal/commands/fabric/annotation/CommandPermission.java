package revxrsal.commands.fabric.annotation;

import net.minecraft.server.command.ServerCommandSource;
import revxrsal.commands.annotation.DistributeOnMethods;
import revxrsal.commands.annotation.NotSender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds a command permission for the given command.
 */
@DistributeOnMethods
@NotSender.ImpliesNotSender
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandPermission {

    /**
     * The permission value. This is passed to {@link ServerCommandSource#hasPermissionLevel(int)}
     *
     * @return The permission value
     */
    int value();

}

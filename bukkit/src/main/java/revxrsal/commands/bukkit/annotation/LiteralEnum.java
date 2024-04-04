package revxrsal.commands.bukkit.annotation;

import revxrsal.commands.bukkit.BukkitBrigadier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that is added to {@code enum} parameters to make
 * them appear as native parameters, instead of regular Minecraft
 * arguments.
 * <p>
 * Note that this behavior can be enabled by default
 * using {@link BukkitBrigadier#showEnumsAsNativeLiterals(boolean)}
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LiteralEnum {
}

package revxrsal.commands.jda.annotation;

import net.dv8tion.jda.api.Permission;
import revxrsal.commands.annotation.DistributeOnMethods;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@DistributeOnMethods
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GuildPermission {

    Permission value();

}

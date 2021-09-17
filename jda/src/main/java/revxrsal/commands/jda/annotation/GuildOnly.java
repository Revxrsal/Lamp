package revxrsal.commands.jda.annotation;

import revxrsal.commands.annotation.DistributeOnMethods;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@DistributeOnMethods
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GuildOnly {}

package revxrsal.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an annotation as distributable on methods. As in, when a class is annotated with an
 * annotation that is distributable on methods, all methods will effectively include this annotation
 * in their functionality.
 * <p>
 * For example, if a class is annotated with {@link SecretCommand}, all methods will be marked as
 * {@link SecretCommand}, without having to annotate each method individually.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributeOnMethods {

}

package revxrsal.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import revxrsal.commands.autocomplete.AutoCompleter;
import revxrsal.commands.autocomplete.SuggestionProvider;

/**
 * Adds tab completion for the command.
 * <p>
 * Each value in {@link #value()} should be separated with a space, and can either be contextual or
 * static:
 * <ul>
 *     <li>
 *         If it is contextual, it should be prefixed with <code>@</code> and
 *         registered with {@link AutoCompleter#registerSuggestion(String, SuggestionProvider)}.
 *     </li>
 *     <li>
 *         If it is static, you can either write up the values right away and separate them
 *         with <code>|</code>, such as <em>1|2|3</em> which will return 1, 2 and 3 when
 *         tab is requested.
 *     </li>
 *     <li>
 *         It is possible to reference the parameter's default auto-completer by using
 *         <code>*</code>, in which the library would automatically use other suggestion
 *         factories to auto-complete it.
 *     </li>
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoComplete {

  /**
   * The tab completion value, by order.
   *
   * @return The tab completion. Check the class documentation for more information.
   */
//    @Pattern("@?([\\w]+)\\|?")
  String value();

}

/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package dev.demeng.pluginbase.commands.autocomplete;

import dev.demeng.pluginbase.commands.command.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Creates a {@link SuggestionProvider} for the given type of parameter. These are most useful in
 * the following cases:
 * <ul>
 *     <li>Creating a suggestion provider for only a specific type of parameters,
 *     for example those with a specific annotation</li>
 *     <li>Creating suggestion providers for a common interface or class</li>
 * </ul>
 * Example: We want to create a suggestion provider for parameters that have
 * a custom annotation
 * <pre>{@code
 * @Target(ElementType.PARAMETER)
 * @Retention(RetentionPolicy.RUNTIME)
 * public @interface WithinRadius {
 *      double value();
 * }
 *
 * public final class WithinRadiusSuggestionFactory implements SuggestionProviderFactory {
 *
 *     @Override public @Nullable SuggestionProvider create(@NotNull CommandParameter parameter) {
 *         if (parameter.getType() != Player.class) return null;
 *         WithinRadius radius = parameter.getAnnotation(WithinRadius.class);
 *         if (radius == null) return null;
 *         return (args, sender, command) -> {
 *             return ((BukkitCommandActor) actor).requirePlayer().getNearbyEntities(
 *                radius.value(), radius.value(), radius.value()
 *             )
 *             .stream().filter(entity -> entity instanceof Player)
 *             .map(Player::getName).collect(Collectors.toList());
 *         };
 *     }
 * }
 * }</pre>
 * <p>
 * Note that {@link SuggestionProviderFactory}ies must be registered
 * with {@link AutoCompleter#registerSuggestionFactory(SuggestionProviderFactory)}
 */
public interface SuggestionProviderFactory {

  /**
   * Creates a {@link SuggestionProvider} for the given parameter. If this parameter is not
   * applicable, {@code null} should be returned.
   *
   * @param parameter Parameter to create for
   * @return The suggestion provider for the parameter, or {@code null} if not applicable.
   */
  @Nullable SuggestionProvider createSuggestionProvider(@NotNull CommandParameter parameter);

  /**
   * Creates a {@link SuggestionProviderFactory} that will return the same provider for all
   * parameters that match a specific type
   *
   * @param type     Type to check for
   * @param provider The provider to use
   * @return The provider factory
   */
  static SuggestionProviderFactory forType(Class<?> type, SuggestionProvider provider) {
    return parameter -> parameter.getType() == type ? provider : null;
  }

  /**
   * Creates a {@link SuggestionProviderFactory} that will return the same provider for all
   * parameters that match or extend a specific type
   *
   * @param type     Type to check for
   * @param provider The provider to use
   * @return The provider factory
   */
  static SuggestionProviderFactory forHierarchyType(Class<?> type, SuggestionProvider provider) {
    return parameter -> parameter.getType() == type || parameter.getType().isAssignableFrom(type)
        ? provider : null;
  }

}

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
package revxrsal.commands.core;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.autocomplete.SuggestionProviderFactory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.util.Strings;

class AutoCompleterAnnotationFactory implements SuggestionProviderFactory {

  private final Map<String, SuggestionProvider> tabProviders;

  public AutoCompleterAnnotationFactory(Map<String, SuggestionProvider> tabProviders) {
    this.tabProviders = tabProviders;
  }

  @Override
  public @Nullable SuggestionProvider createSuggestionProvider(
      @NotNull CommandParameter parameter) {
    AutoComplete ann = parameter.getDeclaringCommand().getAnnotation(AutoComplete.class);
    if (ann != null) {
      return parseTabAnnotation(ann, parameter.getCommandIndex());
    }
    return null;
  }

  private SuggestionProvider parseTabAnnotation(@NotNull AutoComplete annotation,
      int commandIndex) {
    if (annotation.value().isEmpty()) {
      return SuggestionProvider.EMPTY;
    }
    String[] values = Strings.SPACE.split(annotation.value());
    try {
      String providerV = values[commandIndex];
      if (providerV.equals("*")) {
        return null;
      } else if (providerV.startsWith("@")) {
        SuggestionProvider provider = tabProviders.get(providerV.substring(1));
        if (provider == null) {
          throw new IllegalStateException(
              "No such tab suggestion provider: " + providerV.substring(1));
        }
        return provider;
      } else {
        List<String> suggestions = Arrays.asList(Strings.VERTICAL_BAR.split(providerV));
        return SuggestionProvider.of(suggestions);
      }
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }
}

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
package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.annotation.Description;
import dev.demeng.pluginbase.commands.annotation.dynamic.AnnotationReplacer;
import dev.demeng.pluginbase.commands.annotation.dynamic.Annotations;
import dev.demeng.pluginbase.commands.util.Collections;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class LocalesAnnotationReplacer implements AnnotationReplacer<Description> {

  // @Description("#{my.message.key}")
  private static final Pattern LOCALE_PATTERN = Pattern.compile("#\\{(?<key>.*)}");

  private final CommandHandler handler;

  public LocalesAnnotationReplacer(CommandHandler handler) {
    this.handler = handler;
  }

  @Override
  public @Nullable Collection<Annotation> replaceAnnotations(@NotNull AnnotatedElement element,
      @NotNull Description annotation) {
    Matcher matcher = LOCALE_PATTERN.matcher(annotation.value());
    if (matcher.matches()) {
      String key = matcher.group("key");
      return Collections.listOf(
          Annotations.create(Description.class, "value", handler.getTranslator().get(key)));
    }
    return null;
  }
}

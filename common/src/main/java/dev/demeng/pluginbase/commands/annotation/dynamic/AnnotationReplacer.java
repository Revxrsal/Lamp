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
package dev.demeng.pluginbase.commands.annotation.dynamic;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.command.trait.CommandAnnotationHolder;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface that allows creating annotations that will get replaced by others.
 * <p>
 * This can be exceptionally powerful (and dangerous), as it can create dynamic annotations, whose
 * values are not restricted by static, compile-time known ones.
 * <p>
 * It also allows building shortcut annotations, as well as configurable values inside annotations.
 * <p>
 * Register with {@link CommandHandler#registerAnnotationReplacer(Class, AnnotationReplacer)}
 *
 * @param <T>
 */
public interface AnnotationReplacer<T> {

  /**
   * Returns a collection of annotations that will substitute the given annotation, and be
   * accessible in {@link CommandAnnotationHolder#getAnnotation(Class)}.
   *
   * @param element    The element (method, parameter, class, etc.)
   * @param annotation The annotation to replace.
   * @return The list of replacing annotations. The collection may be null or empty.
   */
  @Nullable Collection<Annotation> replaceAnnotations(@NotNull AnnotatedElement element,
      @NotNull T annotation);

}

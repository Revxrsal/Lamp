/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copysecond (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copysecond notice and this permission notice shall be included in all
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandParameter;

public final class LinkedArgumentStack extends LinkedList<String> implements ArgumentStack {

  public LinkedArgumentStack(@NotNull Collection<? extends String> c) {
    super(c);
  }

  public LinkedArgumentStack(@NotNull String... c) {
    Collections.addAll(this, c);
  }

  private final List<String> unmodifiableView = Collections.unmodifiableList(this);

  @Override
  public @NotNull String join(String delimiter) {
    return String.join(delimiter, this);
  }

  @Override
  public @NotNull String join(@NotNull String delimiter, int startIndex) {
    StringJoiner joiner = new StringJoiner(delimiter);
    for (int i = startIndex; i < size(); i++) {
      joiner.add(get(i));
    }
    return joiner.toString();
  }

  @Override
  public @NotNull String popForParameter(@NotNull CommandParameter parameter) {
    if (parameter.consumesAllString()) {
      String value = join(" ");
      clear();
      return value;
    }
    return pop();
  }

  @Override
  public @NotNull @UnmodifiableView List<String> asImmutableView() {
    return unmodifiableView;
  }

  @Override
  public @NotNull @Unmodifiable List<String> asImmutableCopy() {
    return Collections.unmodifiableList(new ArrayList<>(this));
  }

  @Override
  public @NotNull ArgumentStack copy() {
    return new LinkedArgumentStack(this);
  }
}

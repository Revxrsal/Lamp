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

import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ArgumentStack;

public class MutableCommandPath extends CommandPath {

  public static @NotNull MutableCommandPath empty() {
    return new MutableCommandPath(new String[0]);
  }

  public MutableCommandPath(String[] path) {
    super(path);
  }

  public MutableCommandPath(ArgumentStack argumentStack) {
    super(argumentStack.toArray(new String[0]));
  }

  public String removeFirst() {
    return path.removeFirst();
  }

  public String removeLast() {
    return path.removeLast();
  }

  public void addFirst(String s) {
    path.addFirst(s.toLowerCase());
  }

  public void addLast(String s) {
    path.addLast(s.toLowerCase());
  }

  public boolean contains(Object o) {
    return o instanceof String ? path.contains(((String) o).toLowerCase()) : path.contains(o);
  }

  public boolean add(String s) {
    return path.add(s.toLowerCase());
  }

  public void clear() {
    path.clear();
  }

  public void add(int index, String element) {
    path.add(index, element.toLowerCase());
  }

  public String peek() {
    return path.peek();
  }

  public String poll() {
    return path.poll();
  }

  public void push(String s) {
    path.push(s);
  }

  public String pop() {
    return path.pop();
  }

  public CommandPath toImmutablePath() {
    return new CommandPath(path.toArray(new String[0]));
  }

  @Override
  public @NotNull Iterator<String> iterator() {
    return path.iterator();
  }

  @Override
  public boolean isMutable() {
    return true;
  }
}

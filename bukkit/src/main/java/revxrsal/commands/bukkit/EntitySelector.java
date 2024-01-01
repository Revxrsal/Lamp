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
package revxrsal.commands.bukkit;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A parameter that allows entity selectors, such as '@a', '@p', '@s', '@r', '@e[name=Foo]'
 * or player names individually.
 * <p>
 * Note that this selector ONLY works on 1.13+. Unfortunately, Bukkit provides no other
 * ways for parsing selectors on older versions.
 */
public interface EntitySelector<E extends Entity> extends List<E> {

    /**
     * Checks if the selector results are identical to the given array
     * of entities.
     * <p>
     * This will ignore the order of entities in this selector as well as
     * the order of the passed array of entities.
     * <p>
     * Example:
     * <pre>
     * {@code
     * EntitySelector<Player> players = [PlayerA, PlayerB]
     *
     * players.containsExactly(PlayerA) ==> false
     * players.containsExactly(PlayerB) ==> false
     * players.containsExactly(PlayerA, PlayerB) ==> true
     * players.containsExactly(PlayerB, PlayerA) ==> true
     * }
     * </pre>
     *
     * @param entities The entities to check if they are all in the list
     * @return true if the selector yields the same collection of entities
     * as the given array.
     */
    boolean containsExactly(@NotNull Entity... entities);

}

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
package revxrsal.commands.bukkit.brigadier;

import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static revxrsal.commands.util.Preconditions.coerceAtLeast;
import static revxrsal.commands.util.Preconditions.coerceAtMost;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.bukkit.core.BukkitHandler;
import revxrsal.commands.util.Primitives;

/**
 * Contains default argument type resolvers for Bukkit
 */
final class DefaultArgTypeResolvers {

  private DefaultArgTypeResolvers() {
  }

  public static final ArgumentTypeResolver STRING = parameter -> {
    if (parameter.consumesAllString()) {
      return greedyString();
    }
    return string();
  };

  public static final ArgumentTypeResolver BOOLEAN = parameter -> BoolArgumentType.bool();

  public static final ArgumentTypeResolver NUMBER = parameter -> {
    @Nullable Range range = parameter.getAnnotation(Range.class);
    Class<?> type = Primitives.wrap(parameter.getType());
    if (type == Integer.class) {
      if (range == null) {
        return integer();
      }
      return integer((int) range.min(), (int) range.max());
    } else if (type == Double.class) {
      if (range == null) {
        return doubleArg();
      }
      return doubleArg(range.min(), range.max());
    } else if (type == Float.class) {
      if (range == null) {
        return floatArg();
      }
      return floatArg((float) range.min(), (float) range.max());
    } else if (type == Long.class) {
      if (range == null) {
        return longArg();
      }
      return longArg((long) range.min(), (long) range.max());
    } else if (type == Short.class) {
      if (range == null) {
        return integer(Short.MIN_VALUE, Short.MAX_VALUE);
      }
      return integer(coerceAtLeast((int) range.min(), Short.MIN_VALUE),
          coerceAtMost((int) range.max(), Short.MAX_VALUE));
    } else if (type == Byte.class) {
      if (range == null) {
        return integer(Byte.MIN_VALUE, Byte.MAX_VALUE);
      }
      return integer(coerceAtLeast((int) range.min(), Byte.MIN_VALUE),
          coerceAtMost((int) range.max(), Byte.MAX_VALUE));
    }
    return null;
  };

  private static final ArgumentType<?> SINGLE_PLAYER = entity(true, true);
  private static final ArgumentType<?> MULTI_PLAYER = entity(false, true);
  private static final ArgumentType<?> MULTI_ENTITY = entity(false, false);

  public static final ArgumentTypeResolver PLAYER = parameter -> SINGLE_PLAYER;

  public static final ArgumentTypeResolver ENTITY_SELECTOR = parameter -> {
    Class<? extends Entity> type = BukkitHandler.getSelectedEntity(parameter.getFullType());
    if (Player.class.isAssignableFrom(type)) // EntitySelector<Player>
    {
      return MULTI_PLAYER;
    }
    return MULTI_ENTITY;
  };

  private static ArgumentType<?> entity(boolean single, boolean playerOnly) {
    return MinecraftArgumentType.ENTITY.create(single, playerOnly);
  }
}

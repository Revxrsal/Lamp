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
package dev.demeng.pluginbase.commands.bukkit.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An enumeration for containing Minecraft's built-in {@link ArgumentType}s
 */
@SuppressWarnings("rawtypes")
public enum MinecraftArgumentType {

  /**
   * A selector, player name, or UUID.
   * <p>
   * Parameters: - boolean single - boolean playerOnly
   */
  ENTITY("ArgumentEntity", boolean.class, boolean.class),

  /**
   * A player, online or not. Can also use a selector, which may match one or more players (but not
   * entities).
   */
  GAME_PROFILE("ArgumentProfile"),

  /**
   * A chat color. One of the names from <a href="https://wiki.vg/Chat#Colors">colors</a>, or
   * {@code reset}. Case-insensitive.
   */
  COLOR("ArgumentChatFormat"),

  /**
   * A JSON Chat component.
   */
  COMPONENT("ArgumentChatComponent"),

  /**
   * A regular message, potentially including selectors.
   */
  MESSAGE("ArgumentChat"),

  /**
   * An NBT value, parsed using JSON-NBT rules. This represents a full NBT tag.
   */
  NBT("ArgumentNBTTag"),

  /**
   * Represents a partial NBT tag, usable in data modify command.
   */
  NBT_TAG("ArgumentNBTBase"),

  /**
   * A path within an NBT value, allowing for array and member accesses.
   */
  NBT_PATH("ArgumentNBTKey"),

  /**
   * A scoreboard objective.
   */
  SCOREBOARD_OBJECTIVE("ArgumentScoreboardObjective"),

  /**
   * A single score criterion.
   */
  OBJECTIVE_CRITERIA("ArgumentScoreboardCriteria"),

  /**
   * A scoreboard operator.
   */
  SCOREBOARD_SLOT("ArgumentScoreboardSlot"),

  /**
   * Something that can join a team. Allows selectors and *.
   */
  SCORE_HOLDER("ArgumentScoreholder"),

  /**
   * The name of a team. Parsed as an unquoted string.
   */
  TEAM("ArgumentScoreboardTeam"),

  /**
   * A scoreboard operator.
   */
  OPERATION("ArgumentMathOperation"),

  /**
   * A particle effect (an identifier with extra information following it for specific particles,
   * mirroring the Particle packet)
   */
  PARTICLE("ArgumentParticle"),

  /**
   * Represents an angle.
   */
  ANGLE("ArgumentAngle"),

  /**
   * A name for an inventory slot.
   */
  ITEM_SLOT("ArgumentInventorySlot"),

  /**
   * An Identifier.
   */
  RESOURCE_LOCATION("ArgumentMinecraftKeyRegistered"),

  /**
   * A potion effect.
   */
  POTION_EFFECT("ArgumentMobEffect"),

  /**
   * Represents a item enchantment.
   */
  ENCHANTMENT("ArgumentEnchantment"),

  /**
   * Represents an entity summon.
   */
  ENTITY_SUMMON("ArgumentEntitySummon"),

  /**
   * Represents a dimension.
   */
  DIMENSION("ArgumentDimension"),

  /**
   * Represents a time duration.
   */
  TIME("ArgumentTime"),

  /**
   * Represents a UUID value.
   *
   * @since Minecraft 1.16
   */
  UUID("ArgumentUUID"),

  /**
   * A location, represented as 3 numbers (which must be integers). May use relative locations with
   * ~
   */
  BLOCK_POS("coordinates.ArgumentPosition"),

  /**
   * A column location, represented as 3 numbers (which must be integers). May use relative
   * locations with ~.
   */
  COLUMN_POS("coordinates.ArgumentVec2I"),

  /**
   * A location, represented as 3 numbers (which may have a decimal point, but will be moved to the
   * center of a block if none is specified). May use relative locations with ~.
   */
  VECTOR_3("coordinates.ArgumentVec3"),

  /**
   * A location, represented as 2 numbers (which may have a decimal point, but will be moved to the
   * center of a block if none is specified). May use relative locations with ~.
   */
  VECTOR_2("coordinates.ArgumentVec2"),

  /**
   * An angle, represented as 2 numbers (which may have a decimal point, but will be moved to the
   * center of a block if none is specified). May use relative locations with ~.
   */
  ROTATION("coordinates.ArgumentRotation"),

  /**
   * A collection of up to 3 axes.
   */
  SWIZZLE("coordinates.ArgumentRotationAxis"),

  /**
   * A block state, optionally including NBT and state information.
   */
  BLOCK_STATE("blocks.ArgumentTile"),

  /**
   * A block, or a block tag.
   */
  BLOCK_PREDICATE("blocks.ArgumentBlockPredicate"),

  /**
   * An item, optionally including NBT.
   */
  ITEM_STACK("item.ArgumentItemStack"),

  /**
   * An item, or an item tag.
   */
  ITEM_PREDICATE("item.ArgumentItemPredicate"),

  /**
   * A function.
   */
  FUNCTION("item.ArgumentTag"),

  /**
   * The entity anchor related to the facing argument in the teleport command, is feet or eyes.
   */
  ENTITY_ANCHOR("ArgumentAnchor"),

  /**
   * An integer range of values with a min and a max.
   */
  INT_RANGE("ArgumentCriterionValue$b"),

  /**
   * A floating-point range of values with a min and a max.
   */
  FLOAT_RANGE("ArgumentCriterionValue$a"),

  /**
   * Template mirror
   *
   * @since Minecraft 1.19
   */
  TEMPLATE_MIRROR("TemplateMirrorArgument"),

  /**
   * Template rotation
   *
   * @since Minecraft 1.19
   */
  TEMPLATE_ROTATION("TemplateRotationArgument");

  private @Nullable ArgumentType<?> argumentType;
  private @Nullable Constructor<? extends ArgumentType> argumentConstructor;
  private final Class<?>[] parameters;

  MinecraftArgumentType(String name, Class<?>... parameters) {
    Class<?> argumentClass = resolveArgumentClass(name);
    this.parameters = parameters;
    if (argumentClass == null) {
      argumentType = null;
      argumentConstructor = null;
      return;
    }
    try {
      argumentConstructor = argumentClass.asSubclass(ArgumentType.class)
          .getDeclaredConstructor(parameters);
      if (!argumentConstructor.isAccessible()) {
        argumentConstructor.setAccessible(true);
      }
      if (parameters.length == 0) {
        argumentType = argumentConstructor.newInstance();
      } else {
        argumentType = null;
      }
    } catch (Throwable e) {
      argumentType = null;
      argumentConstructor = null;
    }
  }

  /**
   * Checks if this argument type is supported in this Minecraft version
   *
   * @return If this is supported
   */
  public boolean isSupported() {
    return argumentConstructor != null;
  }

  /**
   * Checks if this argument type requires parameters
   *
   * @return If this requires parameters
   */
  public boolean requiresParameters() {
    return parameters.length != 0;
  }

  /**
   * Returns the argument type represented by this enum value, otherwise throws an exception
   *
   * @param <T> The argument type
   * @return The argument type
   * @throws IllegalArgumentException if not supported in this version
   * @throws IllegalArgumentException if this argument requires arguments. See
   *                                  {@link #create(Object...)}
   */
  public @NotNull <T> ArgumentType<T> get() {
    if (argumentConstructor == null) {
      throw new IllegalArgumentException(
          "Argument type '" + name().toLowerCase() + "' is not available on this version.");
    }
    if (argumentType != null) {
      return (ArgumentType<T>) argumentType;
    }
    throw new IllegalArgumentException(
        "This argument type requires " + parameters.length + " parameter(s) of type(s) " +
            Arrays.stream(parameters).map(Class::getName).collect(Collectors.joining(", "))
            + ". Use #create() instead.");
  }

  /**
   * Creates an instance of this argument type
   *
   * @param arguments Arguments to construct the argument type with
   * @param <T>       The argument ttype
   * @return The created argument type.
   * @throws IllegalArgumentException if not supported in this version
   */
  @SneakyThrows
  public @NotNull <T> ArgumentType<T> create(Object... arguments) {
    if (argumentConstructor == null) {
      throw new IllegalArgumentException(
          "Argument type '" + name().toLowerCase() + "' is not available on this version.");
    }
    if (argumentType != null && arguments.length == 0) {
      return (ArgumentType<T>) argumentType;
    }
    return argumentConstructor.newInstance(arguments);
  }

  /**
   * Returns the argument type represented by this enum value, wrapped inside an {@link Optional}
   *
   * @param <T> The argument type
   * @return The argument type optional
   * @throws IllegalArgumentException if this argument requires arguments. See
   *                                  {@link #createIfPresent(Object...)}
   */
  public @NotNull <T> Optional<ArgumentType<T>> getIfPresent() {
    if (argumentConstructor == null) {
      return Optional.empty();
    }
    if (argumentType != null) {
      return Optional.of((ArgumentType<T>) argumentType);
    }
    throw new IllegalArgumentException(
        "This argument type requires " + parameters.length + " parameter(s) of type(s) " +
            Arrays.stream(parameters).map(Class::getName).collect(Collectors.joining(", "))
            + ". Use #create() instead.");
  }

  /**
   * Creates an instance of this argument type, wrapped in an optional.
   *
   * @param arguments Arguments to construct the argument type with
   * @param <T>       The argument ttype
   * @return The created argument type optional.
   */
  @SneakyThrows
  public @NotNull <T> Optional<ArgumentType<T>> createIfPresent(Object... arguments) {
    if (argumentConstructor == null) {
      return Optional.empty();
    }
    if (argumentType != null && arguments.length == 0) {
      return Optional.of((ArgumentType<T>) argumentType);
    }
    return Optional.of(argumentConstructor.newInstance(arguments));
  }

  private static @Nullable Class<?> resolveArgumentClass(String name) {
    try {
      if (ReflectionUtil.minecraftVersion() > 16) {
        return ReflectionUtil.mcClass("commands.arguments." + name);
      } else {
        String stripped;
        if (name.lastIndexOf('.') != -1) {
          stripped = name.substring(name.lastIndexOf('.'));
        } else {
          stripped = name;
        }
        return ReflectionUtil.nmsClass(stripped);
      }
    } catch (Throwable t) {
      return null;
    }
  }

  public static void ensureSetup() {
    // do nothing - this is only called to trigger the static initializer
  }
}

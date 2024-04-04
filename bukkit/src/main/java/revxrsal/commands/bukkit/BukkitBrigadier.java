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

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.annotation.LiteralEnum;
import revxrsal.commands.bukkit.brigadier.ArgumentTypeResolver;
import revxrsal.commands.bukkit.brigadier.MinecraftArgumentType;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;

/**
 * Represents the Brigadier hook for Bukkit
 */
public interface BukkitBrigadier {

    /**
     * Registers a custom {@link ArgumentTypeResolver} that constructs {@link ArgumentType}s
     * for parameters. This has access to information about the parameter being resolved,
     * such as type or annotations.
     *
     * @param resolver Resolver to register.
     */
    void registerArgumentTypeResolver(@NotNull ArgumentTypeResolver resolver);

    /**
     * Registers a custom {@link ArgumentTypeResolver} that constructs {@link ArgumentType}s
     * for parameters. This has access to information about the parameter being resolved,
     * such as type or annotations.
     *
     * @param priority Priority to register the resolver the in. The lower the value, the
     *                 higher the priority. Can be used to override default behavior in
     *                 certain argument types.
     * @param resolver Resolver to register.
     */
    void registerArgumentTypeResolver(int priority, @NotNull ArgumentTypeResolver resolver);

    /**
     * Registers an argument type resolver for the given class. This
     * will include subclasses as well.
     *
     * @param type     Type to register for
     * @param resolver The argument type resolver
     */
    void bind(@NotNull Class<?> type, @NotNull ArgumentTypeResolver resolver);

    /**
     * Registers an argument type for the given class. This will
     * include subclasses as well.
     *
     * @param type         Type to register for
     * @param argumentType The argument type to register
     * @see revxrsal.commands.bukkit.brigadier.MinecraftArgumentType
     */
    void bind(@NotNull Class<?> type, @NotNull ArgumentType<?> argumentType);

    /**
     * Registers an argument type for the given class. This will
     * include subclasses as well.
     *
     * @param type         Type to register for
     * @param argumentType The argument type to register
     * @see revxrsal.commands.bukkit.brigadier.MinecraftArgumentType
     */
    void bind(@NotNull Class<?> type, @NotNull MinecraftArgumentType argumentType);

    /**
     * Returns the argument type corresponding to the given parameter. If
     * no resolver is able to handle this parameter, {@link StringArgumentType#greedyString()}
     * will be returned.
     *
     * @param parameter Parameter to got for
     * @return The argument type
     */
    @NotNull ArgumentType<?> getArgumentType(@NotNull CommandParameter parameter);

    /**
     * Wraps Brigadier's command sender with the platform's appropriate {@link CommandActor}
     *
     * @param commandSource Source to wrap
     * @return The wrapped command source
     */
    @NotNull CommandActor wrapSource(@NotNull Object commandSource);

    /**
     * Enables the native player completion. This means player completions
     * that are identical to vanilla Minecraft commands, prompt selectors
     * such as {@code @e[player=...]} and client-side error validation.
     * <p>
     * This feature is enabled by default. To disable it, it has to
     * be called <em>before</em> registering.
     */
    void disableNativePlayerCompletion();

    /**
     * Tests whether native player completions are enabled or not.
     *
     * @return If native player completion is enabled or not.
     * @see #disableNativePlayerCompletion()
     */
    boolean isNativePlayerCompletionEnabled();

    /**
     * Whether should enums be shown as native literals, instead of regular
     * arguments.
     * <p>
     * Note that it is possible to apply this to individual parameters
     * using {@link LiteralEnum}
     *
     * @param show If they should be shown as native or not
     */
    void showEnumsAsNativeLiterals(boolean show);

    /**
     * Tests whether should enums be shown as native literals
     *
     * @return If enums should be rendered as native literals
     * @see LiteralEnum
     */
    boolean isShowEnumsAsNativeLiterals();

    /**
     * Registers the command handler's brigadier
     */
    void register();

    /**
     * Returns the command handler that instantiated this Brigadier
     * instance.
     *
     * @return The command handler
     */
    @NotNull BukkitCommandHandler getCommandHandler();

}

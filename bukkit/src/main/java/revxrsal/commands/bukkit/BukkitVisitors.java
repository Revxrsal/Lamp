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

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.bukkit.annotation.FallbackPrefix;
import revxrsal.commands.bukkit.brigadier.BrigadierRegistryHook;
import revxrsal.commands.bukkit.brigadier.BukkitArgumentTypes;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.bukkit.hooks.BukkitCommandHooks;
import revxrsal.commands.bukkit.parameters.*;
import revxrsal.commands.bukkit.sender.BukkitPermissionFactory;
import revxrsal.commands.bukkit.sender.BukkitSenderResolver;
import revxrsal.commands.bukkit.util.BukkitVersion;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.parameter.ContextParameter;

import static revxrsal.commands.bukkit.util.BukkitUtils.legacyColorize;
import static revxrsal.commands.bukkit.util.BukkitVersion.isBrigadierSupported;

/**
 * Includes modular building blocks for hooking into the Bukkit
 * platform.
 * <p>
 * Accept individual functions using {@link Lamp.Builder#accept(LampBuilderVisitor)}
 */
public final class BukkitVisitors {

    /**
     * Makes the default format for {@link CommandActor#reply(String)} and {@link CommandActor#error(String)}
     * take the legacy ampersand ChatColor-coded format
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> legacyColorCodes() {
        return builder -> builder
                .defaultMessageSender((actor, message) -> actor.sendRawMessage(legacyColorize(message)))
                .defaultErrorSender((actor, message) -> actor.sendRawMessage(legacyColorize("&c" + message)));
    }

    /**
     * Handles the default Bukkit exceptions
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> bukkitExceptionHandler() {
        //noinspection unchecked
        return builder -> builder.exceptionHandler((CommandExceptionHandler<A>) new BukkitExceptionHandler());
    }

    /**
     * Resolves the sender type {@link CommandSender}, {@link Player} and {@link ConsoleCommandSender}
     * for parameters that come first in the command.
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> bukkitSenderResolver() {
        return builder -> builder.senderResolver(new BukkitSenderResolver());
    }

    /**
     * Registers the following parameter types:
     * <ul>
     *     <li>{@link Player}</li>
     *     <li>{@link OfflinePlayer}</li>
     *     <li>{@link World}</li>
     *     <li>{@link EntitySelector}</li>
     * </ul>
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> bukkitParameterTypes() {
        return builder -> {
            builder.parameterTypes()
                    .addParameterTypeLast(Player.class, new PlayerParameterType())
                    .addParameterTypeLast(OfflinePlayer.class, new OfflinePlayerParameterType())
                    .addParameterTypeLast(World.class, new WorldParameterType())
                    .addParameterTypeFactoryLast(new EntitySelectorParameterTypeFactory());
            if (BukkitVersion.isBrigadierSupported())
                builder.parameterTypes()
                        .addParameterTypeLast(Entity.class, new EntityParameterType());
        };
    }

    /**
     * Adds a registration hook that injects Lamp commands into Bukkit
     *
     * @param plugin The plugin instance to bind commands to
     * @return The visitor
     */
    public static @NotNull LampBuilderVisitor<BukkitCommandActor> registrationHooks(@NotNull JavaPlugin plugin) {
        return registrationHooks(plugin, ActorFactory.defaultFactory());
    }

    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> registrationHooks(
            @NotNull JavaPlugin plugin,
            @NotNull ActorFactory<A> actorFactory
    ) {
        return registrationHooks(plugin, actorFactory, plugin.getName());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Bukkit.
     * <p>
     * This function allows to specify a custom {@link ActorFactory} to
     * use custom implementations of {@link BukkitCommandActor}
     *
     * @param plugin                The plugin instance to bind commands to
     * @param actorFactory          The actor factory. This allows for creating custom {@link BukkitCommandActor}
     *                              implementations
     * @param defaultFallbackPrefix The default fallback prefix to use (which Bukkit uses to
     *                              prevent conflicts). Note that this can be
     *                              overridden on a per-command basis using {@link FallbackPrefix @FallbackPrefix}
     * @return The visitor
     */
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> registrationHooks(
            @NotNull JavaPlugin plugin,
            @NotNull ActorFactory<A> actorFactory,
            @NotNull String defaultFallbackPrefix
    ) {
        BukkitCommandHooks<A> hooks = new BukkitCommandHooks<>(plugin, actorFactory, defaultFallbackPrefix);
        return builder -> builder.hooks()
                .onCommandRegistered(hooks);
    }

    /**
     * Adds {@link Plugin} dependencies and type resolvers
     *
     * @param plugin Plugin to supply
     * @param <A>    The actor type
     * @return The visitor
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> pluginContextParameters(JavaPlugin plugin) {
        return builder -> {
            builder.parameterTypes().addContextParameterLast(Plugin.class, (parameter, context) -> plugin);
            builder.parameterTypes().addContextParameterLast(plugin.getClass(), (ContextParameter) (parameter, context) -> plugin);
            builder.dependency(Plugin.class, plugin);
            builder.dependency((Class) plugin.getClass(), plugin);
        };
    }

    /**
     * Adds support for the {@link CommandPermission} annotation
     *
     * @param <A> The actor type
     * @return This visitor
     */
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> bukkitPermissions() {
        return builder -> builder
                .permissionFactory(BukkitPermissionFactory.INSTANCE);
    }

    /**
     * Adds a registration hook that injects Lamp commands into Bukkit's Brigadier.
     * <p>
     * This function allows to specify a custom {@link ActorFactory} to
     * use custom implementations of {@link BukkitCommandActor}
     *
     * @param plugin The plugin instance to bind commands to
     * @return The visitor
     */
    public static @NotNull LampBuilderVisitor<BukkitCommandActor> brigadier(
            @NotNull JavaPlugin plugin
    ) {
        ArgumentTypes.Builder<BukkitCommandActor> builder = BukkitArgumentTypes.builder();
        return brigadier(plugin, builder.build(), ActorFactory.defaultFactory());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Bukkit's Brigadier.
     *
     * @param argumentTypes The argument types registry. See {@link BukkitArgumentTypes} for
     *                      Bukkit types
     * @param plugin        The plugin instance to bind commands to
     * @return The visitor
     */
    public static @NotNull LampBuilderVisitor<BukkitCommandActor> brigadier(
            @NotNull JavaPlugin plugin,
            @NotNull ArgumentTypes<? super BukkitCommandActor> argumentTypes
    ) {
        return brigadier(plugin, argumentTypes, ActorFactory.defaultFactory());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Bukkit's Brigadier.
     * <p>
     * This function allows to specify a custom {@link ActorFactory} to
     * use custom implementations of {@link BukkitCommandActor}
     *
     * @param plugin        The plugin instance to bind commands to
     * @param argumentTypes The argument types registry. See {@link BukkitArgumentTypes} for
     *                      Bukkit types
     * @param actorFactory  The actor factory. This allows for creating custom {@link BukkitCommandActor}
     *                      implementations
     * @return The visitor
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> brigadier(
            @NotNull JavaPlugin plugin,
            @NotNull ArgumentTypes<? super A> argumentTypes,
            @NotNull ActorFactory<A> actorFactory
    ) {
        if (isBrigadierSupported()) {
            return builder -> builder.hooks()
                    .onCommandRegistered(new BrigadierRegistryHook<>(((ArgumentTypes) argumentTypes), actorFactory, plugin));
        }
        return LampBuilderVisitor.nothing();
    }
}

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
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.brigadier.BrigadierRegistryHook;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.bukkit.hooks.BukkitCommandHooks;
import revxrsal.commands.bukkit.parameters.EntitySelectorParameterTypeFactory;
import revxrsal.commands.bukkit.parameters.OfflinePlayerParameterType;
import revxrsal.commands.bukkit.parameters.PlayerParameterType;
import revxrsal.commands.bukkit.parameters.WorldParameterType;
import revxrsal.commands.bukkit.sender.BukkitPermissionFactory;
import revxrsal.commands.bukkit.sender.BukkitSenderResolver;
import revxrsal.commands.exception.CommandExceptionHandler;

import static revxrsal.commands.bukkit.util.BukkitUtils.legacyColorize;
import static revxrsal.commands.bukkit.util.BukkitVersion.isBrigadierSupported;

public final class BukkitLamp {

    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> legacyColorCodes() {
        return builder -> builder
                .defaultMessageSender((actor, message) -> actor.sendRawMessage(legacyColorize(message)))
                .defaultErrorSender((actor, message) -> actor.sendRawMessage(legacyColorize("&c" + message)));
    }

    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> bukkitExceptionHandler() {
        return builder -> builder.exceptionHandler((CommandExceptionHandler<A>) new BukkitExceptionHandler());
    }

    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> bukkitSenderResolver() {
        return builder -> builder.senderResolver(new BukkitSenderResolver());
    }

    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> bukkitParameterTypes() {
        return builder -> builder.parameterTypes()
                .addParameterTypeLast(Player.class, new PlayerParameterType())
                .addParameterTypeLast(OfflinePlayer.class, new OfflinePlayerParameterType())
                .addParameterTypeLast(World.class, new WorldParameterType())
                .addParameterTypeFactoryLast(new EntitySelectorParameterTypeFactory());
    }

    public static @NotNull LampBuilderVisitor<BukkitCommandActor> registrationHooks(@NotNull JavaPlugin plugin) {
        return registrationHooks(plugin, ActorFactory.defaultFactory());
    }

    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> registrationHooks(@NotNull JavaPlugin plugin, @NotNull ActorFactory<A> factory) {
        BukkitCommandHooks hooks = new BukkitCommandHooks(plugin, factory);
        return builder -> builder.hooks()
                .onCommandRegistered(hooks)
                .onCommandUnregistered(hooks);
    }

    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> pluginContextParameters(JavaPlugin plugin) {
        return builder -> {
            builder.parameterTypes().addContextParameterLast(Plugin.class, (parameter, input, context) -> plugin);
            builder.dependency(Plugin.class, plugin);
        };
    }

    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> bukkitPermissions() {
        return builder -> builder
                .permissionFactory(BukkitPermissionFactory.INSTANCE);
    }

    public static @NotNull LampBuilderVisitor<BukkitCommandActor> brigadier(
            @NotNull JavaPlugin plugin
    ) {
        ArgumentTypes.Builder<BukkitCommandActor> builder = BukkitArgumentTypes.builder();
        return brigadier(plugin, builder.build(), ActorFactory.defaultFactory());
    }

    public static @NotNull LampBuilderVisitor<BukkitCommandActor> brigadier(
            @NotNull JavaPlugin plugin,
            @NotNull ArgumentTypes<? super BukkitCommandActor> argumentTypes
    ) {
        return brigadier(plugin, argumentTypes, ActorFactory.defaultFactory());
    }

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A extends BukkitCommandActor> Lamp.Builder<A> defaultBuilder(
            @NotNull JavaPlugin plugin,
            @NotNull ArgumentTypes<? super A> argumentTypes,
            @NotNull ActorFactory<A> actorFactory
    ) {
        return Lamp.builder(BukkitCommandActor.class)
                .accept(legacyColorCodes())
                .accept(bukkitSenderResolver())
                .accept(bukkitParameterTypes())
                .accept(bukkitExceptionHandler())
                .accept(bukkitPermissions())
                .accept(registrationHooks(plugin))
                .accept(brigadier(plugin, (ArgumentTypes) argumentTypes, actorFactory))
                .accept(pluginContextParameters(plugin));
    }

    public static Lamp.Builder<BukkitCommandActor> defaultBuilder(
            @NotNull JavaPlugin plugin,
            @NotNull ArgumentTypes<BukkitCommandActor> argumentTypes
    ) {
        return defaultBuilder(plugin, argumentTypes, ActorFactory.defaultFactory());
    }

    public static Lamp.Builder<BukkitCommandActor> defaultBuilder(@NotNull JavaPlugin plugin) {
        ArgumentTypes.Builder<BukkitCommandActor> argumentTypes = BukkitArgumentTypes.builder();
        return defaultBuilder(plugin, argumentTypes.build());
    }
}

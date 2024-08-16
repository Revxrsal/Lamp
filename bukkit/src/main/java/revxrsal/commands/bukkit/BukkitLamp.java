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

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.bukkit.hooks.BukkitCommandHooks;
import revxrsal.commands.bukkit.parameters.*;
import revxrsal.commands.bukkit.sender.BukkitSenderResolver;
import revxrsal.commands.exception.CommandExceptionHandler;

import static revxrsal.commands.bukkit.util.BukkitUtils.legacyColorize;

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

    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> registrationHooks(@NotNull JavaPlugin plugin) {
        BukkitCommandHooks hooks = new BukkitCommandHooks(plugin);
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

    public static Lamp.Builder<BukkitCommandActor> defaultBuilder(@NotNull JavaPlugin plugin) {
        return Lamp.builder(BukkitCommandActor.class)
                .accept(legacyColorCodes())
                .accept(bukkitSenderResolver())
                .accept(bukkitParameterTypes())
                .accept(bukkitExceptionHandler())
                .accept(registrationHooks(plugin))
                .accept(pluginContextParameters(plugin));
    }
}

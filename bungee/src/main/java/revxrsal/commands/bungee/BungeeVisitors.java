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
package revxrsal.commands.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.bungee.actor.ActorFactory;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.bungee.exception.BungeeExceptionHandler;
import revxrsal.commands.bungee.hooks.BungeeCommandHooks;
import revxrsal.commands.bungee.parameters.ProxiedPlayerParameterType;
import revxrsal.commands.bungee.sender.BungeePermissionFactory;
import revxrsal.commands.bungee.sender.BungeeSenderResolver;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.parameter.ContextParameter;

import static revxrsal.commands.bungee.util.BungeeUtils.legacyColorize;

/**
 * Includes modular building blocks for hooking into the Bungee
 * platform.
 * <p>
 * Accept individual functions using {@link Lamp.Builder#accept(LampBuilderVisitor)}
 */
public final class BungeeVisitors {

    /**
     * Makes the default format for {@link CommandActor#reply(String)} and {@link CommandActor#error(String)}
     * take the legacy ampersand ChatColor-coded format
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends BungeeCommandActor> @NotNull LampBuilderVisitor<A> legacyColorCodes() {
        return builder -> builder
                .defaultMessageSender((actor, message) -> actor.sendRawMessage(legacyColorize(message)))
                .defaultErrorSender((actor, message) -> actor.sendRawMessage(legacyColorize("&c" + message)));
    }

    /**
     * Handles the default Bungee exceptions
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends BungeeCommandActor> @NotNull LampBuilderVisitor<A> bungeeExceptionHandler() {
        //noinspection unchecked
        return builder -> builder.exceptionHandler((CommandExceptionHandler<A>) new BungeeExceptionHandler());
    }

    /**
     * Resolves the sender type {@link CommandSender} and {@link ProxiedPlayer}
     * for parameters that come first in the command.
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends BungeeCommandActor> @NotNull LampBuilderVisitor<A> bungeeSenderResolver() {
        return builder -> builder.senderResolver(new BungeeSenderResolver());
    }

    /**
     * Registers the following parameter types:
     * <ul>
     *     <li>{@link ProxiedPlayer}</li>
     * </ul>
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends BungeeCommandActor> @NotNull LampBuilderVisitor<A> bungeeParameterTypes() {
        return builder -> builder.parameterTypes()
                .addParameterTypeLast(ProxiedPlayer.class, new ProxiedPlayerParameterType());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Bungee
     *
     * @param plugin The plugin instance to bind commands to
     * @return The visitor
     */
    public static @NotNull LampBuilderVisitor<BungeeCommandActor> registrationHooks(@NotNull Plugin plugin) {
        return registrationHooks(plugin, ActorFactory.defaultFactory());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Bungee.
     * <p>
     * This function allows to specify a custom {@link ActorFactory} to
     * use custom implementations of {@link BungeeCommandActor}
     *
     * @param plugin       The plugin instance to bind commands to
     * @param actorFactory The actor factory. This allows for creating custom {@link BungeeCommandActor}
     *                     implementations
     * @return The visitor
     */
    public static <A extends BungeeCommandActor> @NotNull LampBuilderVisitor<A> registrationHooks(@NotNull Plugin plugin, @NotNull ActorFactory<A> actorFactory) {
        BungeeCommandHooks hooks = new BungeeCommandHooks(plugin, actorFactory);
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
    public static <A extends BungeeCommandActor> @NotNull LampBuilderVisitor<A> pluginContextParameters(Plugin plugin) {
        return builder -> {
            builder.parameterTypes().addContextParameterLast(Plugin.class, (parameter, input, context) -> plugin);
            builder.parameterTypes().addContextParameterLast(plugin.getClass(), (ContextParameter) (parameter, input, context) -> plugin);
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
    public static <A extends BungeeCommandActor> @NotNull LampBuilderVisitor<A> bungeePermissions() {
        return builder -> builder.permissionFactory(BungeePermissionFactory.INSTANCE);
    }

}

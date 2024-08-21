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
package revxrsal.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.parameter.ContextParameter;
import revxrsal.commands.velocity.actor.VelocityCommandActor;
import revxrsal.commands.velocity.annotation.CommandPermission;
import revxrsal.commands.velocity.exception.VelocityExceptionHandler;
import revxrsal.commands.velocity.hooks.VelocityCommandHooks;
import revxrsal.commands.velocity.parameters.PlayerParameterType;
import revxrsal.commands.velocity.sender.VelocityPermissionFactory;
import revxrsal.commands.velocity.sender.VelocitySenderResolver;

import static revxrsal.commands.velocity.util.VelocityUtils.legacyColorize;

/**
 * Includes modular building blocks for hooking into the Velocity
 * platform.
 * <p>
 * Accept individual functions using {@link Lamp.Builder#accept(LampBuilderVisitor)}
 */
public final class VelocityVisitors {

    /**
     * Makes the default format for {@link CommandActor#reply(String)} and {@link CommandActor#error(String)}
     * take the legacy ampersand ChatColor-coded format
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends VelocityCommandActor> @NotNull LampBuilderVisitor<A> legacyColorCodes() {
        return builder -> builder
                .defaultMessageSender((actor, message) -> actor.source().sendMessage(legacyColorize(message)))
                .defaultErrorSender((actor, message) -> actor.sendRawMessage(legacyColorize("&c" + message)));
    }

    /**
     * Handles the default Velocity exceptions
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends VelocityCommandActor> @NotNull LampBuilderVisitor<A> velocityExceptionHandler() {
        return builder -> builder.exceptionHandler((CommandExceptionHandler<A>) new VelocityExceptionHandler());
    }

    /**
     * Resolves the sender type {@link CommandSource} and {@link Player}
     * for parameters that come first in the command.
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends VelocityCommandActor> @NotNull LampBuilderVisitor<A> velocitySenderResolver() {
        return builder -> builder.senderResolver(new VelocitySenderResolver());
    }

    /**
     * Registers the following parameter types:
     * <ul>
     *     <li>{@link Player}</li>
     * </ul>
     *
     * @param server The server
     * @param <A>    The actor type
     * @return The visitor
     */
    public static <A extends VelocityCommandActor> @NotNull LampBuilderVisitor<A> velocityParameterTypes(@NotNull ProxyServer server) {
        return builder -> builder.parameterTypes()
                .addParameterTypeLast(Player.class, new PlayerParameterType(server));
    }

    /**
     * Adds a registration hook that injects Lamp commands into Velocity.
     *
     * @param config The {@link VelocityLampConfig} instance
     * @param <A>    The actor type
     * @return The visitor
     */
    public static <A extends VelocityCommandActor> @NotNull LampBuilderVisitor<A> registrationHooks(
            @NotNull VelocityLampConfig<A> config
    ) {
        VelocityCommandHooks<A> hooks = new VelocityCommandHooks<>(config);
        return builder -> builder.hooks()
                .onCommandRegistered(hooks);
    }

    /**
     * Adds dependencies and type resolvers for the given plugin object
     *
     * @param plugin Plugin to supply
     * @param <A>    The actor type
     * @return The visitor
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A extends VelocityCommandActor> @NotNull LampBuilderVisitor<A> pluginContextParameters(Object plugin) {
        return builder -> {
            builder.parameterTypes().addContextParameterLast(plugin.getClass(), (ContextParameter) (parameter, input, context) -> plugin);
            builder.dependency((Class) plugin.getClass(), plugin);
        };
    }

    /**
     * Adds support for the {@link CommandPermission} annotation
     *
     * @param <A> The actor type
     * @return This visitor
     */
    public static <A extends VelocityCommandActor> @NotNull LampBuilderVisitor<A> velocityPermissions() {
        return builder -> builder.permissionFactory(VelocityPermissionFactory.INSTANCE);
    }
}

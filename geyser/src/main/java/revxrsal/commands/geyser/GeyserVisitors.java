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
package revxrsal.commands.geyser;

import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.entity.type.player.GeyserPlayerEntity;
import org.geysermc.geyser.api.extension.Extension;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.geyser.actor.ActorFactory;
import revxrsal.commands.geyser.actor.GeyserCommandActor;
import revxrsal.commands.geyser.annotation.CommandPermission;
import revxrsal.commands.geyser.exception.GeyserExceptionHandler;
import revxrsal.commands.geyser.hooks.GeyserCommandHooks;
import revxrsal.commands.geyser.parameters.GeyserConnectionParameterType;
import revxrsal.commands.geyser.parameters.GeyserPlayerParameterType;
import revxrsal.commands.geyser.sender.GeyserPermissionFactory;
import revxrsal.commands.geyser.sender.GeyserSenderResolver;
import revxrsal.commands.parameter.ContextParameter;

import static revxrsal.commands.geyser.util.GeyserUtils.legacyColorize;

/**
 * Includes modular building blocks for hooking into the Geyser
 * platform.
 * <p>
 * Accept individual functions using {@link Lamp.Builder#accept(LampBuilderVisitor)}
 */
public final class GeyserVisitors {

    private GeyserVisitors() {}

    /**
     * Makes the default format for {@link CommandActor#reply(String)} and {@link CommandActor#error(String)}
     * take the legacy ampersand ChatColor-coded format
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends GeyserCommandActor> @NotNull LampBuilderVisitor<A> legacyColorCodes() {
        return builder -> builder
                .defaultMessageSender((actor, message) -> actor.sendRawMessage(legacyColorize(message)))
                .defaultErrorSender((actor, message) -> actor.sendRawMessage(legacyColorize("&c" + message)));
    }

    /**
     * Handles the default Geyser exceptions
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends GeyserCommandActor> @NotNull LampBuilderVisitor<A> geyserExceptionHandler() {
        //noinspection unchecked
        return builder -> builder.exceptionHandler((CommandExceptionHandler<A>) new GeyserExceptionHandler());
    }

    /**
     * Resolves the sender types {@link CommandSource}, {@link GeyserConnection}
     * and {@link GeyserPlayerEntity} for parameters that come first in the command.
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends GeyserCommandActor> @NotNull LampBuilderVisitor<A> geyserSenderResolver() {
        return builder -> builder.senderResolver(GeyserSenderResolver.INSTANCE);
    }

    /**
     * Registers the following parameter types:
     * <ul>
     *     <li>{@link GeyserConnection}</li>
     *     <li>{@link GeyserPlayerEntity}</li>
     * </ul>
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends GeyserCommandActor> @NotNull LampBuilderVisitor<A> geyserParameterTypes() {
        return builder -> builder.parameterTypes()
                .addParameterTypeLast(GeyserConnection.class, new GeyserConnectionParameterType())
                .addParameterTypeLast(GeyserPlayerEntity.class, new GeyserPlayerParameterType());
    }

    /**
     * Adds support for the {@link CommandPermission} annotation
     *
     * @param <A> The actor type
     * @return This visitor
     */
    public static <A extends GeyserCommandActor> @NotNull LampBuilderVisitor<A> geyserPermissions() {
        return builder -> builder.permissionFactory(GeyserPermissionFactory.INSTANCE);
    }

    /**
     * Adds {@link Extension} dependencies and type resolvers
     *
     * @param extension Extension to supply
     * @param <A>       The actor type
     * @return The visitor
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A extends GeyserCommandActor> @NotNull LampBuilderVisitor<A> extensionContextParameters(@NotNull Extension extension) {
        return builder -> {
            builder.parameterTypes().addContextParameterLast(Extension.class, (parameter, context) -> extension);
            builder.parameterTypes().addContextParameterLast(extension.getClass(), (ContextParameter) (parameter, context) -> extension);
            builder.dependency(Extension.class, extension);
            builder.dependency((Class) extension.getClass(), extension);
        };
    }

    /**
     * Adds a registration hook that injects Lamp commands into Geyser
     *
     * @param extension The extension instance to bind commands to
     * @return The visitor
     */
    public static @NotNull LampBuilderVisitor<GeyserCommandActor> registrationHooks(@NotNull Extension extension) {
        return registrationHooks(extension, ActorFactory.defaultFactory());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Geyser.
     * <p>
     * This function allows to specify a custom {@link ActorFactory} to
     * use custom implementations of {@link GeyserCommandActor}
     *
     * @param extension    The extension instance to bind commands to
     * @param actorFactory The actor factory. This allows for creating custom {@link GeyserCommandActor}
     *                     implementations
     * @param <A>          The actor type
     * @return The visitor
     */
    public static <A extends GeyserCommandActor> @NotNull LampBuilderVisitor<A> registrationHooks(
            @NotNull Extension extension,
            @NotNull ActorFactory<A> actorFactory
    ) {
        GeyserCommandHooks<A> hooks = new GeyserCommandHooks<>(extension, actorFactory);
        return builder -> builder.hooks()
                .onCommandRegistered(hooks);
    }
}

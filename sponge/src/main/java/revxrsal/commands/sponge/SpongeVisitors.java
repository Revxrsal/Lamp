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
package revxrsal.commands.sponge;

import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.parameter.ContextParameter;
import revxrsal.commands.response.ResponseHandler;
import revxrsal.commands.sponge.actor.ActorFactory;
import revxrsal.commands.sponge.actor.SpongeCommandActor;
import revxrsal.commands.sponge.annotation.CommandPermission;
import revxrsal.commands.sponge.exception.SpongeExceptionHandler;
import revxrsal.commands.sponge.hooks.SpongeCommandHooks;
import revxrsal.commands.sponge.parameters.SelectorParameterType;
import revxrsal.commands.sponge.parameters.ServerPlayerParameterType;
import revxrsal.commands.sponge.parameters.ServerWorldParameterType;
import revxrsal.commands.sponge.sender.SpongePermissionFactory;
import revxrsal.commands.sponge.sender.SpongeSenderResolver;

import static revxrsal.commands.response.ResponseHandler.Factory.forTypeAndSubclasses;
import static revxrsal.commands.sponge.util.SpongeUtils.legacyColorize;

/**
 * Includes modular building blocks for hooking into the Sponge
 * platform.
 * <p>
 * Accept individual functions using {@link Lamp.Builder#accept(LampBuilderVisitor)}
 */
public final class SpongeVisitors {

    /**
     * Makes the default format for {@link CommandActor#reply(String)} and {@link CommandActor#error(String)}
     * take the legacy ampersand ChatColor-coded format
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends SpongeCommandActor> @NotNull LampBuilderVisitor<A> legacyColorCodes() {
        return builder -> builder
                .defaultMessageSender((actor, message) -> actor.sendRawMessage(legacyColorize(message)))
                .defaultErrorSender((actor, message) -> actor.sendRawMessage(legacyColorize("&c" + message)));
    }

    /**
     * Registers {@link ResponseHandler}s for {@link ComponentLike adventure components}
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends SpongeCommandActor> @NotNull LampBuilderVisitor<A> componentResponseHandlers() {
        return builder -> builder
                .responseHandler(forTypeAndSubclasses(ComponentLike.class, (response, context) -> context.actor().reply(response)));
    }

    /**
     * Handles the default Sponge exceptions
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends SpongeCommandActor> @NotNull LampBuilderVisitor<A> spongeExceptionHandler() {
        //noinspection unchecked
        return builder -> builder.exceptionHandler((CommandExceptionHandler<A>) new SpongeExceptionHandler());
    }

    /**
     * Resolves the sender type {@link CommandCause}, {@link ServerPlayer} and {@link SystemSubject}
     * for parameters that come first in the command.
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends SpongeCommandActor> @NotNull LampBuilderVisitor<A> spongeSenderResolver() {
        return builder -> builder.senderResolver(new SpongeSenderResolver());
    }

    /**
     * Registers the following parameter types:
     * <ul>
     *     <li>{@link Selector}</li>
     *     <li>{@link ServerPlayer}</li>
     *     <li>{@link ServerWorld}</li>
     * </ul>
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends SpongeCommandActor> @NotNull LampBuilderVisitor<A> spongeParameterTypes() {
        return builder -> builder.parameterTypes()
                .addParameterTypeLast(ServerPlayer.class, new ServerPlayerParameterType())
                .addParameterTypeLast(Selector.class, new SelectorParameterType())
                .addParameterTypeLast(ServerWorld.class, new ServerWorldParameterType());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Sponge
     *
     * @param plugin The plugin instance to bind commands to
     * @return The visitor
     */
    public static @NotNull LampBuilderVisitor<SpongeCommandActor> registrationHooks(@NotNull Object plugin) {
        return registrationHooks(plugin, ActorFactory.defaultFactory());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Sponge.
     * <p>
     * This function allows to specify a custom {@link ActorFactory} to
     * use custom implementations of {@link SpongeCommandActor}
     *
     * @param plugin       The plugin instance to bind commands to
     * @param actorFactory The actor factory. This allows for creating custom {@link SpongeCommandActor}
     *                     implementations
     * @return The visitor
     */
    public static <A extends SpongeCommandActor> @NotNull LampBuilderVisitor<A> registrationHooks(
            @NotNull Object plugin,
            @NotNull ActorFactory<A> actorFactory
    ) {
        SpongeCommandHooks<A> hooks = new SpongeCommandHooks<>(plugin, actorFactory);
        return builder -> builder.hooks().onCommandRegistered(hooks);
    }

    /**
     * Adds plugin dependencies and type resolvers
     *
     * @param plugin Plugin to supply
     * @param <A>    The actor type
     * @return The visitor
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A extends SpongeCommandActor> @NotNull LampBuilderVisitor<A> pluginContextParameters(Object plugin) {
        return builder -> {
            builder.parameterTypes().addContextParameterLast(plugin.getClass(), (ContextParameter) (parameter, context) -> plugin);
            builder.dependency((Class) plugin.getClass(), plugin);
        };
    }

    /**
     * Adds support for the {@link CommandPermission} annotation
     *
     * @param <A> The actor type
     * @return This visitor
     */
    public static <A extends SpongeCommandActor> @NotNull LampBuilderVisitor<A> spongePermissions() {
        return builder -> builder
                .permissionFactory(SpongePermissionFactory.INSTANCE);
    }
}

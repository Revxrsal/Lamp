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
package revxrsal.commands.fabric;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.fabric.actor.FabricCommandActor;
import revxrsal.commands.fabric.annotation.CommandPermission;
import revxrsal.commands.fabric.exception.FabricExceptionHandler;
import revxrsal.commands.fabric.hooks.FabricCommandHooks;
import revxrsal.commands.fabric.parameters.PlayerParameterType;
import revxrsal.commands.fabric.parameters.WorldParameterType;
import revxrsal.commands.fabric.sender.FabricPermissionFactory;
import revxrsal.commands.fabric.sender.FabricSenderResolver;
import revxrsal.commands.parameter.ContextParameter;

import static revxrsal.commands.fabric.util.FabricUtils.legacyColorize;

/**
 * Includes modular building blocks for hooking into the Fabric
 * platform.
 * <p>
 * Accept individual functions using {@link Lamp.Builder#accept(LampBuilderVisitor)}
 */
public final class FabricVisitors {

    /**
     * Makes the default format for {@link CommandActor#reply(String)} and {@link CommandActor#error(String)}
     * take the legacy ampersand ChatColor-coded format
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends FabricCommandActor> @NotNull LampBuilderVisitor<A> legacyColorCodes() {
        return builder -> builder
                .defaultMessageSender((actor, message) -> actor.source().sendMessage(legacyColorize(message)))
                .defaultErrorSender((actor, message) -> actor.sendRawMessage(legacyColorize("&c" + message)));
    }

    /**
     * Handles the default Fabric exceptions
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends FabricCommandActor> @NotNull LampBuilderVisitor<A> fabricExceptionHandler() {
        //noinspection unchecked
        return builder -> builder.exceptionHandler((CommandExceptionHandler<A>) new FabricExceptionHandler());
    }

    /**
     * Resolves the sender type {@link ServerCommandSource} and {@link ServerPlayerEntity}
     * for parameters that come first in the command.
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends FabricCommandActor> @NotNull LampBuilderVisitor<A> fabricSenderResolver() {
        return builder -> builder.senderResolver(new FabricSenderResolver());
    }

    /**
     * Registers the following parameter types:
     * <ul>
     *     <li>{@link ServerPlayerEntity}</li>
     *     <li>{@link World}</li>
     * </ul>
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends FabricCommandActor> @NotNull LampBuilderVisitor<A> fabricParameterTypes() {
        return builder -> builder.parameterTypes()
                .addParameterTypeLast(ServerPlayerEntity.class, new PlayerParameterType())
                .addParameterTypeLast(World.class, new WorldParameterType());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Fabric.
     *
     * @param config The {@link FabricLampConfig} instance
     * @param <A>    The actor type
     * @return The visitor
     */
    public static <A extends FabricCommandActor> @NotNull LampBuilderVisitor<A> registrationHooks(
            @NotNull FabricLampConfig<A> config
    ) {
        FabricCommandHooks<A> hooks = new FabricCommandHooks<>(config);
        return builder -> builder.hooks()
                .onCommandRegistered(hooks);
    }

    /**
     * Adds support for the {@link CommandPermission} annotation
     *
     * @param <A> The actor type
     * @return This visitor
     */
    public static <A extends FabricCommandActor> @NotNull LampBuilderVisitor<A> fabricPermissions() {
        return builder -> builder.permissionFactory(FabricPermissionFactory.INSTANCE);
    }
}

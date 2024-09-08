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
package revxrsal.commands.minestom;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.socket.Server;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.minestom.actor.ActorFactory;
import revxrsal.commands.minestom.actor.MinestomCommandActor;
import revxrsal.commands.minestom.annotation.CommandPermission;
import revxrsal.commands.minestom.argument.ArgumentTypes;
import revxrsal.commands.minestom.argument.MinestomArgumentTypes;
import revxrsal.commands.minestom.exception.MinestomExceptionHandler;
import revxrsal.commands.minestom.hooks.MinestomCommandHooks;
import revxrsal.commands.minestom.parameters.InstanceParameterType;
import revxrsal.commands.minestom.parameters.PlayerParameterType;
import revxrsal.commands.minestom.sender.MinestomPermissionFactory;
import revxrsal.commands.minestom.sender.MinestomSenderResolver;

import static revxrsal.commands.minestom.MinestomStubParameterType.stubParameterType;
import static revxrsal.commands.minestom.util.MinestomUtils.legacyColorize;

/**
 * Includes modular building blocks for hooking into the Minestom
 * platform.
 * <p>
 * Accept individual functions using {@link Lamp.Builder#accept(LampBuilderVisitor)}
 */
public final class MinestomVisitors {

    /**
     * Makes the default format for {@link CommandActor#reply(String)} and {@link CommandActor#error(String)}
     * take the legacy ampersand ChatColor-coded format
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends MinestomCommandActor> @NotNull LampBuilderVisitor<A> legacyColorCodes() {
        return builder -> builder
                .defaultMessageSender((actor, message) -> actor.sendRawMessage(legacyColorize(message)))
                .defaultErrorSender((actor, message) -> actor.sendRawMessage(legacyColorize("&c" + message)));
    }

    /**
     * Handles the default Minestom exceptions
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends MinestomCommandActor> @NotNull LampBuilderVisitor<A> minestomExceptionHandler() {
        //noinspection unchecked
        return builder -> builder.exceptionHandler((CommandExceptionHandler<A>) new MinestomExceptionHandler());
    }

    /**
     * Resolves the sender type {@link CommandSender}, {@link ConsoleSender} and {@link Player}
     * for parameters that come first in the command.
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends MinestomCommandActor> @NotNull LampBuilderVisitor<A> minestomSenderResolver() {
        return builder -> builder.senderResolver(new MinestomSenderResolver());
    }

    /**
     * Registers the following parameter types:
     * <ul>
     *     <li>{@link Player}</li>
     *     <li>{@link Instance}</li>
     *     <li>{@link EntityFinder}</li>
     *     <li>{@link ItemStack}</li>
     *     <li>{@link Component}</li>
     *     <li>{@link BinaryTag}</li>
     *     <li>{@link CompoundBinaryTag}</li>
     * </ul>
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends MinestomCommandActor> @NotNull LampBuilderVisitor<A> minestomParameterTypes() {
        return builder -> builder.parameterTypes()
                .addParameterTypeLast(Player.class, new PlayerParameterType())
                .addParameterTypeLast(Instance.class, new InstanceParameterType())
                .addParameterTypeLast(EntityFinder.class, stubParameterType())
                .addParameterTypeLast(ItemStack.class, stubParameterType())
                .addParameterTypeLast(Component.class, stubParameterType())
                .addParameterTypeLast(BinaryTag.class, stubParameterType())
                .addParameterTypeLast(CompoundBinaryTag.class, stubParameterType());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Minestom
     *
     * @return The visitor
     */
    public static @NotNull LampBuilderVisitor<MinestomCommandActor> registrationHooks() {
        return registrationHooks(ActorFactory.defaultFactory());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Minestom.
     * <p>
     * This function allows to specify a custom {@link ActorFactory} to
     * use custom implementations of {@link MinestomCommandActor}
     *
     * @param actorFactory The actor factory. This allows for creating custom {@link MinestomCommandActor}
     *                     implementations
     * @return The visitor
     */
    public static <A extends MinestomCommandActor> @NotNull LampBuilderVisitor<A> registrationHooks(
            @NotNull ActorFactory<A> actorFactory
    ) {
        return registrationHooks(actorFactory, MinestomArgumentTypes.<A>builder().build());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Minestom.
     * <p>
     * This function allows to specify a custom {@link ActorFactory} to
     * use custom implementations of {@link MinestomCommandActor}
     *
     * @param actorFactory The actor factory. This allows for creating custom {@link MinestomCommandActor}
     *                     implementations
     * @return The visitor
     */
    public static <A extends MinestomCommandActor> @NotNull LampBuilderVisitor<A> registrationHooks(
            @NotNull ActorFactory<A> actorFactory,
            @NotNull ArgumentTypes<A> argumentTypes
    ) {
        MinestomCommandHooks<A> hooks = new MinestomCommandHooks<>(actorFactory, argumentTypes);
        return builder -> builder.hooks()
                .onCommandRegistered(hooks);
    }

    /**
     * Adds relevant Minestom context parameters
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends MinestomCommandActor> @NotNull LampBuilderVisitor<A> minestomContextParameters() {
        return builder -> {
            builder.parameterTypes().addContextParameterLast(Server.class, (a, b) -> MinecraftServer.getServer());
            builder.parameterTypes().addContextParameterLast(InstanceManager.class, (a, b) -> MinecraftServer.getInstanceManager());
        };
    }

    /**
     * Adds support for the {@link CommandPermission} annotation
     *
     * @param <A> The actor type
     * @return This visitor
     */
    public static <A extends MinestomCommandActor> @NotNull LampBuilderVisitor<A> minestomPermissions() {
        return builder -> builder.permissionFactory(MinestomPermissionFactory.INSTANCE);
    }
}

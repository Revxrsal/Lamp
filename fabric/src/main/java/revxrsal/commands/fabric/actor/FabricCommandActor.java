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
package revxrsal.commands.fabric.actor;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.fabric.exception.SenderNotConsoleException;
import revxrsal.commands.fabric.exception.SenderNotPlayerException;
import revxrsal.commands.fabric.util.FabricUtils;
import revxrsal.commands.process.MessageSender;

/**
 * Represents a Fabric {@link CommandActor} that wraps {@link ServerCommandSource}
 */
public interface FabricCommandActor extends CommandActor {

    /**
     * Returns the underlying {@link ServerCommandSource} of this actor
     *
     * @return The revxrsal.commands.bungee.sender
     */
    @NotNull ServerCommandSource source();

    /**
     * Tests whether is this actor a player or not
     *
     * @return Is this a player or not
     */
    default boolean isPlayer() {
        return source().getEntity() instanceof ServerPlayerEntity;
    }

    /**
     * Tests whether is this actor the console or not
     *
     * @return Is this the console or not
     */
    default boolean isConsole() {
        return !isPlayer();
    }

    /**
     * Returns this actor as a {@link ServerPlayerEntity} if it is a player,
     * otherwise returns {@code null}.
     *
     * @return The sender as a player, or null.
     */
    @Nullable
    default ServerPlayerEntity asPlayer() {
        return isPlayer() ? (ServerPlayerEntity) source().getEntity() : null;
    }

    /**
     * Returns this actor as a {@link ServerPlayerEntity} if it is a player,
     * otherwise throws a {@link SenderNotPlayerException}.
     *
     * @return The actor as a player
     * @throws SenderNotPlayerException if not a player
     */
    @NotNull
    default ServerPlayerEntity requirePlayer() throws SenderNotPlayerException {
        if (!isPlayer())
            throw new SenderNotPlayerException();
        return (ServerPlayerEntity) source().getEntity();
    }

    /**
     * Returns this actor source if it is the console, otherwise throws
     * a {@link SenderNotConsoleException}.
     *
     * @return The actor as a player
     * @throws SenderNotConsoleException if not a console
     */
    @NotNull
    default ServerCommandSource requireConsole() throws SenderNotConsoleException {
        if (!isPlayer())
            throw new SenderNotConsoleException();
        return source();
    }

    /**
     * Sends the given component to this actor.
     * <p>
     * Note that this may be delegated to an underlying {@link MessageSender},
     * as specified in an {@link ActorFactory}.
     *
     * @param message The message to send
     */
    void reply(@NotNull Text message);

    /**
     * Sends the given component to this error.
     * <p>
     * Note that this may be delegated to an underlying {@link MessageSender},
     * as specified in an {@link ActorFactory}.
     *
     * @param message The message to send
     */
    void error(@NotNull Text message);

    /**
     * Prints the given component to this actor. This function does
     * not delegate sending, but invokes {@link ServerCommandSource#sendMessage(Text)}
     * directly
     *
     * @param message The message to send
     */
    default void sendRawMessage(@NotNull Text message) {
        source().sendMessage(message);
    }

    /**
     * Prints the given component to this actor as an error. This function does
     * not delegate sending, but invokes {@link ServerCommandSource#sendMessage(Text)}
     * directly
     *
     * @param message The message to send
     */
    default void sendRawError(@NotNull Text message) {
        source().sendMessage(message.copy().styled(v -> v.withColor(Formatting.RED)));
    }

    /**
     * Sends the given message to the actor, with legacy color-coding.
     * <p>
     * This function does
     * not delegate sending, but invokes {@link ServerCommandSource#sendMessage(Text)}
     * directly
     *
     * @param message Message to send
     */
    @Override
    default void sendRawMessage(@NotNull String message) {
        source().sendMessage(FabricUtils.legacyColorize(message));
    }

    /**
     * Sends the given message to the actor as an error, with legacy color-coding.
     * <p>
     * This function does
     * not delegate sending, but invokes {@link ServerCommandSource#sendMessage(Text)}
     * directly
     *
     * @param message Message to send
     */
    @Override
    default void sendRawError(@NotNull String message) {
        source().sendMessage(FabricUtils.legacyColorize("&c" + message));
    }

    /**
     * Returns the {@link Lamp} instance that constructed this actor.
     *
     * @return The {@link Lamp} instance
     */
    @Override Lamp<FabricCommandActor> lamp();

    /**
     * Returns the name of this actor. Varies depending on the
     * platform.
     *
     * @return The actor name
     */
    @Override @NotNull
    default String name() {
        return isConsole() ? "Console" : requirePlayer().getEntityName();
    }
}

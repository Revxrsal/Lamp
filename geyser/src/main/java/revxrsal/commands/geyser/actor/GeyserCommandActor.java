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
package revxrsal.commands.geyser.actor;

import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.entity.type.player.GeyserPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.geyser.exception.SenderNotBedrockPlayerException;
import revxrsal.commands.geyser.exception.SenderNotConsoleException;

/**
 * Represents a Geyser {@link CommandActor} that wraps {@link CommandSource}
 */
public interface GeyserCommandActor extends CommandActor {

    /**
     * Returns the underlying {@link CommandSource} of this actor
     *
     * @return The source
     */
    @NotNull CommandSource source();

    /**
     * Tests whether is this actor a player or not. This includes
     * both Java and Bedrock players.
     *
     * @return Is this a player or not
     */
    default boolean isPlayer() {
        return source().playerUuid() != null;
    }

    /**
     * Tests whether is this actor a Bedrock player connected to
     * this Geyser instance or not
     *
     * @return Is this a Bedrock player or not
     */
    default boolean isBedrockPlayer() {
        return source().connection() != null;
    }

    /**
     * Tests whether is this actor the console or not
     *
     * @return Is this the console or not
     */
    default boolean isConsole() {
        return source().isConsole();
    }

    /**
     * Returns this actor as a {@link GeyserConnection} if it is a Bedrock player,
     * otherwise returns {@code null}.
     *
     * @return The source as a connection, or null.
     */
    default @Nullable GeyserConnection asConnection() {
        return source().connection();
    }

    /**
     * Returns this actor as a {@link GeyserConnection} if it is a Bedrock player,
     * otherwise throws a {@link SenderNotBedrockPlayerException}.
     *
     * @return The actor as a connection
     * @throws SenderNotBedrockPlayerException if not a Bedrock player
     */
    default @NotNull GeyserConnection requireConnection() throws SenderNotBedrockPlayerException {
        GeyserConnection connection = source().connection();
        if (connection == null)
            throw new SenderNotBedrockPlayerException();
        return connection;
    }

    /**
     * Returns this actor as a {@link GeyserPlayerEntity} if it is a Bedrock player,
     * otherwise returns {@code null}.
     *
     * @return The source as a player, or null.
     */
    default @Nullable GeyserPlayerEntity asPlayer() {
        GeyserConnection connection = asConnection();
        return connection == null ? null : connection.playerEntity();
    }

    /**
     * Returns this actor as a {@link GeyserPlayerEntity} if it is a Bedrock player,
     * otherwise throws a {@link SenderNotBedrockPlayerException}.
     *
     * @return The actor as a player
     * @throws SenderNotBedrockPlayerException if not a Bedrock player
     */
    default @NotNull GeyserPlayerEntity requirePlayer() throws SenderNotBedrockPlayerException {
        return requireConnection().playerEntity();
    }

    /**
     * Returns the {@link CommandSource} of this actor if it is the console,
     * otherwise throws a {@link SenderNotConsoleException}.
     *
     * @return The console source
     * @throws SenderNotConsoleException if not the console
     */
    default @NotNull CommandSource requireConsole() throws SenderNotConsoleException {
        if (!isConsole())
            throw new SenderNotConsoleException();
        return source();
    }

    /**
     * Returns the {@link Lamp} instance that constructed this actor.
     *
     * @return The {@link Lamp} instance
     */
    @Override @NotNull Lamp<GeyserCommandActor> lamp();
}

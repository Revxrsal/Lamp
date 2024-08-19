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
package revxrsal.commands.velocity.actor;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.velocity.exception.SenderNotConsoleException;
import revxrsal.commands.velocity.exception.SenderNotPlayerException;
import revxrsal.commands.velocity.util.VelocityUtils;

/**
 * Represents a Velocity {@link CommandActor} that wraps {@link CommandSource}
 */
public interface VelocityCommandActor extends CommandActor {

    /**
     * Returns the underlying {@link CommandSource} of this actor
     *
     * @return The revxrsal.commands.bungee.sender
     */
    @NotNull CommandSource sender();

    /**
     * Tests whether is this actor a player or not
     *
     * @return Is this a player or not
     */
    default boolean isPlayer() {
        return sender() instanceof Player;
    }

    /**
     * Tests whether is this actor the console or not
     *
     * @return Is this the console or not
     */
    default boolean isConsole() {
        return sender() instanceof ConsoleCommandSource;
    }

    /**
     * Returns this actor as a {@link Player} if it is a player,
     * otherwise returns {@code null}.
     *
     * @return The sender as a player, or null.
     */
    @Nullable
    default Player asPlayer() {
        return isPlayer() ? (Player) sender() : null;
    }

    /**
     * Returns this actor as a {@link Player} if it is a player,
     * otherwise throws a {@link SenderNotPlayerException}.
     *
     * @return The actor as a player
     * @throws SenderNotPlayerException if not a player
     */
    @NotNull
    default Player requirePlayer() throws SenderNotPlayerException {
        if (!isPlayer())
            throw new SenderNotPlayerException();
        return (Player) sender();
    }

    /**
     * Returns this actor as a {@link ConsoleCommandSource} if it is the console,
     * otherwise throws a {@link SenderNotConsoleException}.
     *
     * @return The actor as a player
     * @throws SenderNotPlayerException if not a player
     */
    @NotNull
    default ConsoleCommandSource requireConsole() throws SenderNotConsoleException {
        if (!isPlayer())
            throw new SenderNotConsoleException();
        return (ConsoleCommandSource) sender();
    }

    void reply(@NotNull ComponentLike message);

    void error(@NotNull ComponentLike message);

    default void sendRawMessage(@NotNull ComponentLike message) {
        sender().sendMessage(message);
    }

    default void sendRawError(@NotNull ComponentLike message) {
        sender().sendMessage(message.asComponent().colorIfAbsent(NamedTextColor.RED));
    }

    @Override
    default void sendRawMessage(@NotNull String message) {
        sender().sendMessage(VelocityUtils.legacyColorize(message));
    }

    @Override
    default void sendRawError(@NotNull String message) {
        sender().sendMessage(VelocityUtils.legacyColorize("&c" + message));
    }

    @Override Lamp<VelocityCommandActor> lamp();

    @Override @NotNull
    default String name() {
        return isConsole() ? "Console" : requirePlayer().getUsername();
    }
}

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
package revxrsal.commands.bungee.actor;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.bungee.exception.SenderNotPlayerException;
import revxrsal.commands.command.CommandActor;

/**
 * Represents a Bungee {@link CommandActor} that wraps {@link CommandSender}
 */
public interface BungeeCommandActor extends CommandActor {

    /**
     * Returns the underlying {@link CommandSender} of this actor
     *
     * @return The sender
     */
    @NotNull CommandSender sender();

    /**
     * Tests whether is this actor a player or not
     *
     * @return Is this a player or not
     */
    default boolean isPlayer() {
        return sender() instanceof ProxiedPlayer;
    }

    /**
     * Returns this actor as a {@link ProxiedPlayer} if it is a player,
     * otherwise returns {@code null}.
     *
     * @return The sender as a player, or null.
     */
    @Nullable
    default ProxiedPlayer asPlayer() {
        return isPlayer() ? (ProxiedPlayer) sender() : null;
    }

    /**
     * Returns this actor as a {@link ProxiedPlayer} if it is a player,
     * otherwise throws a {@link SenderNotPlayerException}.
     *
     * @return The actor as a player
     * @throws SenderNotPlayerException if not a player
     */
    @NotNull
    default ProxiedPlayer requirePlayer() throws SenderNotPlayerException {
        if (!isPlayer())
            throw new SenderNotPlayerException();
        return (ProxiedPlayer) sender();
    }

    @Override
    default void sendRawMessage(@NotNull String message) {
        sender().sendMessage(new TextComponent(message));
    }

    @Override
    default void sendRawError(@NotNull String message) {
        sender().sendMessage(new TextComponent(ChatColor.RED + message));
    }

    /**
     * Returns the {@link Lamp} instance that constructed this actor.
     *
     * @return The {@link Lamp} instance
     */
    @Override Lamp<BungeeCommandActor> lamp();

    /**
     * Returns the name of this actor. Varies depending on the
     * platform.
     *
     * @return The actor name
     */
    @Override @NotNull
    default String name() {
        return sender().getName();
    }
}

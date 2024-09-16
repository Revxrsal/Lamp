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
package revxrsal.commands.bukkit.actor;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.exception.SenderNotConsoleException;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;
import revxrsal.commands.command.CommandActor;

import java.util.Optional;

/**
 * Represents a Bukkit {@link CommandActor} that wraps {@link CommandSender}
 */
public interface BukkitCommandActor extends CommandActor {

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
        return sender() instanceof Player;
    }

    /**
     * Tests whether is this actor the console or not
     *
     * @return Is this the console or not
     */
    default boolean isConsole() {
        return sender() instanceof ConsoleCommandSender;
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
     * Returns this actor as a {@link ConsoleCommandSender} if it is a player,
     * otherwise throws a {@link SenderNotConsoleException}.
     *
     * @return The actor as console
     * @throws SenderNotConsoleException if not a console
     */
    @NotNull default ConsoleCommandSender requireConsole() throws SenderNotConsoleException {
        if (!isConsole())
            throw new SenderNotConsoleException();
        return (ConsoleCommandSender) sender();
    }

    /**
     * Prints the given component to this actor. This function does
     * not delegate sending, but invokes {@link CommandSender#sendMessage(String)}
     * directly
     *
     * @param message The message to send
     */
    @Override
    default void sendRawMessage(@NotNull String message) {
        sender().sendMessage(message);
    }

    /**
     * Prints the given component to this actor as an error. This function does
     * not delegate sending, but invokes {@link CommandSender#sendMessage(String)}
     * directly
     *
     * @param message The message to send
     */
    @Override
    default void sendRawError(@NotNull String message) {
        sender().sendMessage(ChatColor.RED + message);
    }

    /**
     * Prints the given component to this actor. This function does
     * not delegate sending, but invokes {@link CommandSender#sendMessage(String)}
     * directly
     *
     * @param message The message to send
     */
    void reply(@NotNull ComponentLike message);

    /**
     * Returns this actor as an adventure {@link Audience}.
     *
     * @return The audience
     */
    @NotNull Optional<Audience> audience();

    /**
     * Returns the {@link Lamp} instance that constructed this actor.
     *
     * @return The {@link Lamp} instance
     */
    @Override Lamp<BukkitCommandActor> lamp();

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

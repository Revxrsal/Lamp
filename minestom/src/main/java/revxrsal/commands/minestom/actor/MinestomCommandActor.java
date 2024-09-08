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
package revxrsal.commands.minestom.actor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.minestom.exception.SenderNotConsoleException;
import revxrsal.commands.minestom.exception.SenderNotPlayerException;
import revxrsal.commands.minestom.util.MinestomUtils;
import revxrsal.commands.process.MessageSender;

/**
 * Represents a Minestom {@link CommandActor} that wraps {@link CommandSender}
 */
public interface MinestomCommandActor extends CommandActor {

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
        return sender() instanceof ConsoleSender;
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
     * Returns this actor as a {@link ConsoleSender} if it is a player,
     * otherwise throws a {@link SenderNotConsoleException}.
     *
     * @return The actor as console
     * @throws SenderNotConsoleException if not a console
     */
    @NotNull default ConsoleSender requireConsole() throws SenderNotConsoleException {
        if (!isConsole())
            throw new SenderNotConsoleException();
        return (ConsoleSender) sender();
    }

    /**
     * Sends the given component to this actor.
     * <p>
     * Note that this may be delegated to an underlying {@link MessageSender},
     * as specified in an {@link ActorFactory}.
     *
     * @param message The message to send
     */
    void reply(@NotNull ComponentLike message);

    /**
     * Sends the given component to this error.
     * <p>
     * Note that this may be delegated to an underlying {@link MessageSender},
     * as specified in an {@link ActorFactory}.
     *
     * @param message The message to send
     */
    void error(@NotNull ComponentLike message);

    /**
     * Prints the given component to this actor. This function does
     * not delegate sending, but invokes {@link CommandSender#sendMessage(Component)}
     * directly
     *
     * @param message The message to send
     */
    default void sendRawMessage(@NotNull ComponentLike message) {
        sender().sendMessage(message);
    }

    /**
     * Prints the given component to this actor as an error. This function does
     * not delegate sending, but invokes {@link CommandSender#sendMessage(Component)}
     * directly
     *
     * @param message The message to send
     */
    default void sendRawError(@NotNull ComponentLike message) {
        sender().sendMessage(message.asComponent().colorIfAbsent(NamedTextColor.RED));
    }


    /**
     * Sends the given message to the actor, with legacy color-coding.
     * <p>
     * This function does
     * not delegate sending, but invokes {@link CommandSender#sendMessage(Component)}
     * directly
     *
     * @param message Message to send
     */
    @Override
    default void sendRawMessage(@NotNull String message) {
        sender().sendMessage(MinestomUtils.legacyColorize(message));
    }

    /**
     * Sends the given message to the actor as an error, with legacy color-coding.
     * <p>
     * This function does
     * not delegate sending, but invokes {@link CommandSender#sendMessage(Component)}
     * directly
     *
     * @param message Message to send
     */
    @Override
    default void sendRawError(@NotNull String message) {
        sender().sendMessage(MinestomUtils.legacyColorize("&c" + message));
    }

    /**
     * Returns the {@link Lamp} instance that constructed this actor.
     *
     * @return The {@link Lamp} instance
     */
    @Override Lamp<MinestomCommandActor> lamp();

    /**
     * Returns the name of this actor. Varies depending on the
     * platform.
     *
     * @return The actor name
     */
    @Override @NotNull
    default String name() {
        if (isPlayer())
            return ((Player) sender()).getUsername();
        return "Console";
    }
}

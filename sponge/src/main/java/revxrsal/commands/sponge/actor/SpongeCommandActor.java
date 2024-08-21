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
package revxrsal.commands.sponge.actor;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Nameable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.process.MessageSender;
import revxrsal.commands.sponge.exception.SenderNotConsoleException;
import revxrsal.commands.sponge.exception.SenderNotPlayerException;

import static revxrsal.commands.sponge.util.SpongeUtils.legacyColorize;

/**
 * Represents a Sponge {@link CommandActor} that wraps {@link CommandCause}
 */
public interface SpongeCommandActor extends CommandActor {

    /**
     * Returns the underlying {@link CommandCause} of this actor
     *
     * @return The command cause
     */
    @NotNull CommandCause cause();

    /**
     * Returns the underlying {@link Subject} of this actor
     *
     * @return The subject
     */
    default @NotNull Subject subject() {
        return cause().subject();
    }

    /**
     * Returns the underlying {@link Subject} of this actor
     *
     * @return The subject
     */
    default @NotNull Audience audience() {
        return cause().audience();
    }

    /**
     * Tests whether is this actor a player or not
     *
     * @return Is this a player or not
     */
    default boolean isPlayer() {
        return subject() instanceof ServerPlayer;
    }

    /**
     * Tests whether is this actor the console or not
     *
     * @return Is this the console or not
     */
    default boolean isConsole() {
        return subject() instanceof SystemSubject;
    }

    /**
     * Returns this actor as a {@link ServerPlayer} if it is a player,
     * otherwise returns {@code null}.
     *
     * @return The sender as a player, or null.
     */
    default @Nullable ServerPlayer asPlayer() {
        return isPlayer() ? (ServerPlayer) subject() : null;
    }

    /**
     * Returns this actor as a {@link ServerPlayer} if it is a player,
     * otherwise throws a {@link SenderNotPlayerException}.
     *
     * @return The actor as a player
     * @throws SenderNotPlayerException if not a player
     */
    default @NotNull ServerPlayer requirePlayer() throws SenderNotPlayerException {
        if (!isPlayer())
            throw new SenderNotPlayerException();
        return (ServerPlayer) subject();
    }

    /**
     * Returns this actor as a {@link SystemSubject} if it is a player,
     * otherwise throws a {@link SenderNotConsoleException}.
     *
     * @return The actor as console
     * @throws SenderNotConsoleException if not a player
     */
    default @NotNull SystemSubject requireConsole() throws SenderNotConsoleException {
        if (!isConsole())
            throw new SenderNotConsoleException();
        return (SystemSubject) subject();
    }

    /**
     * Prints the given component to this actor. This function does
     * not delegate sending, but invokes {@link Audience#sendMessage(Component)}
     * directly
     *
     * @param message The message to send
     */
    @Override
    default void sendRawMessage(@NotNull String message) {
        cause().audience().sendMessage(legacyColorize(message));
    }

    /**
     * Prints the given component to this actor as an error. This function does
     * not delegate sending, but invokes {@link Audience#sendMessage(Component)}
     * directly
     *
     * @param message The message to send
     */
    @Override
    default void sendRawError(@NotNull String message) {
        cause().audience().sendMessage(legacyColorize("&c" + message));
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
     * not delegate sending, but invokes {@link Audience#sendMessage(Component)}
     * directly
     *
     * @param message The message to send
     */
    default void sendRawMessage(@NotNull ComponentLike message) {
        audience().sendMessage(message);
    }

    /**
     * Prints the given component to this actor as an error. This function does
     * not delegate sending, but invokes {@link Audience#sendMessage(Component)}
     * directly
     *
     * @param message The message to send
     */
    default void sendRawError(@NotNull ComponentLike message) {
        audience().sendMessage(message.asComponent().colorIfAbsent(NamedTextColor.RED));
    }

    /**
     * Returns the {@link Lamp} instance that constructed this actor.
     *
     * @return The {@link Lamp} instance
     */
    @Override Lamp<SpongeCommandActor> lamp();

    /**
     * Returns the name of this actor. This tries to find the most suitable
     * name if the actor does not provide such one
     *
     * @return The actor name
     */
    @Override @NotNull
    default String name() {
        if (subject() instanceof Nameable n) {
            return n.name();
        } else if (isConsole()) {
            return "Console";
        } else {
            return subject().friendlyIdentifier().orElseGet(() -> subject().toString());
        }
    }
}

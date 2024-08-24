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
package revxrsal.commands.sponge.parameters;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.sponge.actor.SpongeCommandActor;
import revxrsal.commands.sponge.exception.InvalidPlayerException;
import revxrsal.commands.stream.MutableStringStream;

import java.util.Optional;

import static revxrsal.commands.util.Collections.map;

/**
 * A parameter type for {@link ServerPlayer} types.
 * <p>
 * If the player inputs {@code me} or {@code self}, the parser will return the
 * executing player (or give an error if the sender is not a player)
 */
public final class ServerPlayerParameterType implements ParameterType<SpongeCommandActor, ServerPlayer> {

    @Override
    public ServerPlayer parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<SpongeCommandActor> context) {
        String name = input.readString();
        if (name.equals("self") || name.equals("me") || name.equals("@s"))
            return context.actor().requirePlayer();
        Optional<ServerPlayer> player = Sponge.server().player(name);
        if (player.isPresent())
            return player.get();
        throw new InvalidPlayerException(name);
    }

    @Override
    public @NotNull SuggestionProvider<SpongeCommandActor> defaultSuggestions() {
        return (input, actor, context) -> map(Sponge.server().onlinePlayers(), ServerPlayer::name);
    }
}

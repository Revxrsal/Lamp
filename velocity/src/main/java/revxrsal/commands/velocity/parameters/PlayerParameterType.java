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
package revxrsal.commands.velocity.parameters;


import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.velocity.actor.VelocityCommandActor;
import revxrsal.commands.velocity.exception.InvalidPlayerException;

import java.util.Optional;

import static revxrsal.commands.util.Collections.map;

/**
 * A parameter type for {@link Player} types.
 * <p>
 * If the player inputs {@code me} or {@code self} or {@code @s}, the parser will
 * return the executing player (or give an error if the sender is not a player)
 */
public record PlayerParameterType(@NotNull ProxyServer server) implements ParameterType<VelocityCommandActor, Player> {

    @Override
    public Player parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<VelocityCommandActor> context) {
        String name = input.readString();
        if (name.equals("self") || name.equals("me") || name.equals("@s"))
            return context.actor().requirePlayer();
        Optional<Player> player = server.getPlayer(name);
        if (player.isPresent())
            return player.get();
        throw new InvalidPlayerException(name);
    }

    @Override public @NotNull SuggestionProvider<VelocityCommandActor> defaultSuggestions() {
        return (input, actor, context) -> map(server.getAllPlayers(), Player::getUsername);
    }
}

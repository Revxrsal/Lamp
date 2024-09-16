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
package revxrsal.commands.minestom.parameters;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.minestom.actor.MinestomCommandActor;
import revxrsal.commands.minestom.exception.InvalidPlayerException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

import static revxrsal.commands.util.Collections.map;

/**
 * A parameter type for {@link Instance} types.
 * <p>
 * If the player inputs {@code me} or {@code self}, the parser will return the
 * executing player (or give an error if the sender is not a player)
 */
public final class InstanceParameterType implements ParameterType<MinestomCommandActor, Instance> {

    @Override
    public Instance parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<MinestomCommandActor> context) {
        String name = input.readString();
        if (name.equals("self") || name.equals("me") || name.equals("@s"))
            return context.actor().requirePlayer().getInstance();
        Instance instance = MinecraftServer.getInstanceManager().getInstances().stream()
                .filter(current -> current.getDimensionName().equals(name))
                .findFirst()
                .orElse(null);
        if (instance != null)
            return instance;
        throw new InvalidPlayerException(name);
    }

    @Override public @NotNull SuggestionProvider<MinestomCommandActor> defaultSuggestions() {
        return (context) -> map(MinecraftServer.getConnectionManager().getOnlinePlayers(), Player::getUsername);
    }
}

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
package revxrsal.commands.geyser.parameters;

import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.geyser.actor.GeyserCommandActor;
import revxrsal.commands.geyser.exception.InvalidPlayerException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

/**
 * A parameter type for {@link GeyserConnection} types.
 * <p>
 * If the player inputs {@code me} or {@code self} or {@code @s}, the parser will
 * return the executing player (or give an error if the sender is not a player)
 */
public final class GeyserConnectionParameterType implements ParameterType<GeyserCommandActor, GeyserConnection> {

    @Override
    public GeyserConnection parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<GeyserCommandActor> context) {
        String name = input.readString();
        return switch (name) {
            case "me", "self", "@s" -> context.actor().requireConnection();
            default -> GeyserApi.api().onlineConnections().stream()
                    .filter(connection -> connection.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new InvalidPlayerException(name));
        };
    }
}

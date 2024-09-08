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
package revxrsal.commands.fabric.parameters;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.fabric.actor.FabricCommandActor;
import revxrsal.commands.fabric.exception.InvalidWorldException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

import java.util.List;

/**
 * A parameter type for {@link ServerPlayerEntity} types.
 * <p>
 * If the player inputs {@code me} or {@code self} or {@code @s}, the parser will
 * return the executing player (or give an error if the sender is not a player)
 */
public class WorldParameterType implements ParameterType<FabricCommandActor, World> {

    private static @Nullable World getWorld(@NotNull MinecraftServer server, @NotNull String name) {
        for (RegistryKey<World> key : server.getWorldRegistryKeys()) {
            if (key.getValue().toString().equalsIgnoreCase(name) || key.getValue().getPath().equalsIgnoreCase(name)) {
                return server.getWorld(key);
            }
        }
        return null;
    }

    @Override
    public World parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<FabricCommandActor> context) {
        String name = input.readString();
        if (name.equals("self") || name.equals("me") || name.equals("@s"))
            return context.actor().requirePlayer().getWorld();
        MinecraftServer server = context.actor().source().getServer();
        World world = getWorld(server, name);
        if (world == null)
            throw new InvalidWorldException(name);
        return world;
    }

    @Override public @NotNull SuggestionProvider<FabricCommandActor> defaultSuggestions() {
        return (input, context) -> {
            MinecraftServer server = context.actor().source().getServer();
            return List.of(server.getPlayerManager().getPlayerNames());
        };
    }
}

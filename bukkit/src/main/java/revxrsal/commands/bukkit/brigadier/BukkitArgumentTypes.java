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
package revxrsal.commands.bukkit.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Single;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.parameters.EntitySelector;
import revxrsal.commands.command.CommandActor;

import java.util.UUID;

import static revxrsal.commands.util.Classes.getFirstGeneric;
import static revxrsal.commands.util.Classes.getRawType;

public final class BukkitArgumentTypes {

    // parameters: single, playersOnly
    private static final ArgumentType<?> SINGLE_PLAYER = MinecraftArgumentType.ENTITY.create(true, true);
    private static final ArgumentType<?> PLAYERS = MinecraftArgumentType.ENTITY.create(false, true);

    private static final ArgumentType<?> SINGLE_ENTITY = MinecraftArgumentType.ENTITY.create(true, false);
    private static final ArgumentType<?> ENTITIES = MinecraftArgumentType.ENTITY.create(false, false);

    /**
     * Creates a new {@link ArgumentTypes.Builder}. This function is primarily for improving
     * type-inference and pleasing the Java compiler. The {@code actorType} parameter
     * is not used.
     *
     * @param actorType Actor type. This allows for better type-inference. The parameter is unused.
     * @param <A>       The actor type
     * @return The newly created builder
     */
    public static @NotNull <A extends CommandActor> ArgumentTypes.Builder<A> builder(
            @SuppressWarnings("unused") Class<A> actorType
    ) {
        return builder();
    }

    @Contract(value = "-> new", pure = true)
    public static @NotNull <A extends CommandActor> ArgumentTypes.Builder<A> builder() {
        ArgumentTypes.Builder<A> builder = ArgumentTypes.builder();
        return builder
                .addTypeLast(UUID.class, MinecraftArgumentType.UUID.get())
                .addTypeLast(OfflinePlayer.class, SINGLE_PLAYER)
                .addTypeLast(Player.class, SINGLE_PLAYER)
                .addTypeLast(Entity.class, SINGLE_ENTITY)
                .addTypeFactoryLast((parameter) -> {
                    if (parameter.type() != EntitySelector.class)
                        return null;
                    Class<? extends Entity> entityType = getRawType(getFirstGeneric(parameter.fullType(), Entity.class))
                            .asSubclass(Entity.class);
                    boolean single = parameter.annotations().contains(Single.class);
                    if (entityType == Player.class)
                        return single ? SINGLE_PLAYER : PLAYERS;
                    else
                        return single ? SINGLE_ENTITY : ENTITIES;
                });
    }
}

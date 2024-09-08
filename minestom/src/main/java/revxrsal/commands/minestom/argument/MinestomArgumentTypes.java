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
package revxrsal.commands.minestom.argument;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;

import java.util.UUID;

public final class MinestomArgumentTypes {

    @Contract(value = "-> new", pure = true)
    public static @NotNull <A extends CommandActor> ArgumentTypes.Builder<A> builder() {
        return ArgumentTypes.<A>builder()
                .addTypeLast(UUID.class, node -> ArgumentType.UUID(node.name()))
                .addTypeLast(EntityFinder.class, node -> ArgumentType.Entity(node.name()))
                .addTypeLast(ItemStack.class, node -> ArgumentType.ItemStack(node.name()))
                .addTypeLast(Component.class, node -> ArgumentType.Component(node.name()))
                .addTypeLast(BinaryTag.class, node -> ArgumentType.NBT(node.name()))
                .addTypeLast(CompoundBinaryTag.class, node -> ArgumentType.NbtCompound(node.name()))
                .addTypeLast(Player.class, node -> {
                    return ArgumentType.Entity(node.name()).onlyPlayers(true).singleEntity(true)
                            .map((sender, finder) -> finder.findFirstPlayer(sender));
                })
                .addTypeLast(Entity.class, node -> {
                    return ArgumentType.Entity(node.name()).singleEntity(true)
                            .map((sender, finder) -> finder.findFirstEntity(sender));
                });
    }

}

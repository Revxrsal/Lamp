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
package revxrsal.commands.bukkit.core;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.MinecraftArgumentTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.brigadier.LampBrigadier;
import revxrsal.commands.bukkit.EntitySelector;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.util.ClassMap;

import java.lang.reflect.Constructor;

final class BukkitBrigadier implements LampBrigadier {

    private static final ArgumentType<?> PLAYERS = entity(false, true);

    private final ClassMap<ArgumentType<?>> argumentTypes = new ClassMap<>();
    private final Commodore commodore;
    private final CommandHandler handler;

    public BukkitBrigadier(Commodore commodore, CommandHandler handler) {
        this.commodore = commodore;
        this.handler = handler;
        argumentTypes.add(Player.class, entity(true, true));
        argumentTypes.add(EntitySelector.class, entity(false, false));
    }

    @Override public @NotNull CommandActor wrapSource(@NotNull Object commandSource) {
        return new BukkitActor(commodore.getBukkitSender(commandSource), handler);
    }

    @Override public @NotNull ClassMap<ArgumentType<?>> getAdditionalArgumentTypes() {
        return argumentTypes;
    }

    @Override public void register(@NotNull LiteralCommandNode<?> node) {
        commodore.register(node);
    }

    @Override public @Nullable ArgumentType<?> getArgumentType(@NotNull CommandParameter parameter) {
        if (EntitySelector.class.isAssignableFrom(parameter.getType())) {
            Class<? extends Entity> type = BukkitHandler.getSelectedEntity(parameter.getFullType());
            if (Player.class.isAssignableFrom(type)) // EntitySelector<Player>
                return PLAYERS;
        }
        return null;
    }

    private static ArgumentType<?> entity(boolean single, boolean playerOnly) {
        return newEntityType(NamespacedKey.minecraft("entity"), single, playerOnly);
    }

    private static ArgumentType<?> newEntityType(NamespacedKey key, Object... args) {
        try {
            final Constructor<? extends ArgumentType<?>> constructor = MinecraftArgumentTypes.getClassByKey(key).getDeclaredConstructor(boolean.class, boolean.class);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}

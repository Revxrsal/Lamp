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
package revxrsal.commands.bukkit.parameters;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.EmptyEntitySelectorException;
import revxrsal.commands.bukkit.exception.MalformedEntitySelectorException;
import revxrsal.commands.bukkit.exception.MoreThanOneEntityException;
import revxrsal.commands.bukkit.util.BukkitVersion;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

import java.util.List;

import static revxrsal.commands.util.Collections.map;

/**
 * A parameter type for {@link Player} types.
 * <p>
 * If the player inputs {@code me} or {@code self}, the parser will return the
 * executing player (or give an error if the sender is not a player)
 */
public final class EntityParameterType implements ParameterType<BukkitCommandActor, Entity> {

    @Override
    public Entity parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> context) {
        String value = input.readString();
        return fromSelector(context.actor().sender(), value);
    }

    @Override public @NotNull SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
        // Brigadier's entity type will handle auto-completions for us :)
        if (BukkitVersion.isBrigadierSupported())
            return SuggestionProvider.empty();
        return (input, context) -> map(Bukkit.getOnlinePlayers(), Player::getName);
    }

    public static @NotNull Entity fromSelector(@NotNull CommandSender sender, @NotNull String selector) {
        try {
            List<Entity> entityList = Bukkit.selectEntities(sender, selector);
            if (entityList.isEmpty())
                throw new EmptyEntitySelectorException(selector);
            if (entityList.size() != 1)
                throw new MoreThanOneEntityException(selector);
            return entityList.get(0);
        } catch (IllegalArgumentException e) {
            throw new MalformedEntitySelectorException(selector, e.getCause().getMessage());
        } catch (NoSuchMethodError e) {
            throw new CommandErrorException("Entity selectors on legacy versions are not supported yet!");
        }
    }
}
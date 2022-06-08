/*
 * This file is part of commodore, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility for using Minecraft's 1.13 'brigadier' library in Bukkit plugins.
 */
interface Commodore {

    /**
     * Gets the current command dispatcher instance.
     *
     * <p>CraftBukkit doesn't use the same dispatcher instance throughout
     * the runtime of the server. The dispatcher instance is completely wiped
     * (and replaced with a new instance) every time new plugins are loaded.</p>
     *
     * @return the command dispatcher
     */
    @SuppressWarnings("rawtypes")
    CommandDispatcher getDispatcher();

    /**
     * Gets the CommandSender associated with the passed CommandWrapperListener.
     *
     * <p>Minecraft calls brigadier command handlers with an instance of CommandWrapperListener,
     * which cannot be accessed without accessing nms code. This method takes
     * an Object as parameter, but the only type actually accepted is those
     * from the S type provided by Minecraft.</p>
     *
     * @param commandWrapperListener the CommandWrapperListener instance from nms.
     * @return the CommandWrapperListener wrapped as a CommandSender.
     */
    CommandSender getBukkitSender(Object commandWrapperListener);

    /**
     * Registers the provided argument data to the dispatcher, against all
     * aliases defined for the {@code command}.
     *
     * <p>Additionally applies the CraftBukkit {@link SuggestionProvider}
     * to all arguments within the node, so ASK_SERVER suggestions can continue
     * to function for the command.</p>
     *
     * <p>Players will only be sent argument data if they pass the provided
     * {@code permissionTest}.</p>
     *
     * @param command the command to read aliases from
     * @param node the argument data
     * @param permissionTest the predicate to check whether players should be sent argument data
     */
    void register(Command command, LiteralCommandNode<?> node, Predicate<? super Player> permissionTest);

    /**
     * Registers the provided argument data to the dispatcher, against all
     * aliases defined for the {@code command}.
     *
     * <p>Additionally applies the CraftBukkit {@link SuggestionProvider}
     * to all arguments within the node, so ASK_SERVER suggestions can continue
     * to function for the command.</p>
     *
     * @param command the command to read aliases from
     * @param node the argument data
     */
    void register(Command command, LiteralCommandNode<?> node);

    /**
     * Registers the provided argument data to the dispatcher.
     *
     * <p>Equivalent to calling
     * {@link CommandDispatcher#register(LiteralArgumentBuilder)}.</p>
     *
     * <p>Prefer using {@link #register(Command, LiteralCommandNode)}.</p>
     *
     * @param node the argument data
     */
    void register(LiteralCommandNode<?> node);

    /**
     * Gets all the aliases known for the given command.
     *
     * <p>This will include the main label, as well as defined aliases, and
     * aliases including the fallback prefix added by Bukkit.</p>
     *
     * @param command the command
     * @return the aliases
     */
    static Collection<String> getAliases(Command command) {
        Objects.requireNonNull(command, "command");
        Stream<String> aliasesStream = Stream.concat(
                Stream.of(command.getLabel()),
                command.getAliases().stream()
        );

        if (command instanceof PluginCommand) {
            String fallbackPrefix = ((PluginCommand) command).getPlugin().getName().toLowerCase().trim();
            aliasesStream = aliasesStream.flatMap(alias -> Stream.of(
                    alias,
                    fallbackPrefix + ":" + alias
            ));
        }

        return aliasesStream.distinct().collect(Collectors.toList());
    }

}

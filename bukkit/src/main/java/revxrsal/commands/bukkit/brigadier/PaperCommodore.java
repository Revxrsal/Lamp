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
package revxrsal.commands.bukkit.brigadier;

import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.command.UnknownCommandEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.core.BukkitCommandExecutor;
import revxrsal.commands.command.ArgumentStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static revxrsal.commands.util.Strings.stripNamespace;

@SuppressWarnings({"rawtypes"})
final class PaperCommodore extends Commodore implements Listener {

    private final Map<String, LiteralCommandNode<?>> commands = new HashMap<>();
    private final BukkitCommandHandler handler;
    private final String fallbackPrefix;

    PaperCommodore(@NotNull BukkitCommandHandler handler) {
        this.handler = handler;
        Plugin plugin = handler.getPlugin();
        fallbackPrefix = plugin.getName().toLowerCase().trim();
        registerListener(plugin);
    }

    private void registerListener(Plugin plugin) {
        // Put each listener in a class, in case one of them fails due to incompatibility.
        Bukkit.getPluginManager().registerEvents(new UnknownCommandListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new CommandRegisterListener(), plugin);
    }

    public final class UnknownCommandListener implements Listener {

        @EventHandler
        public void onUnknownCommand(UnknownCommandEvent event) {
            ArgumentStack args = ArgumentStack.parse(
                    stripNamespace(fallbackPrefix, event.getCommandLine())
            );
            if (commands.containsKey(args.getFirst())) {
                event.message(null);
                BukkitCommandActor actor = BukkitCommandActor.wrap(event.getSender(), handler);
                try {
                    // This will automatically fail, we can then easily get the
                    // exception message and overwrite it.
                    handler.dispatch(actor, args);
                } catch (Throwable t) {
                    handler.getExceptionHandler().handleException(t, actor);
                }
            }
        }
    }

    public final class CommandRegisterListener implements Listener {
        @EventHandler
        public void onCommandRegistered(CommandRegisteredEvent<?> event) {
            if (!(event.getCommand() instanceof PluginCommand)) {
                return;
            }
            PluginCommand pCommand = (PluginCommand) event.getCommand();
            if (!(pCommand.getExecutor() instanceof BukkitCommandExecutor)) {
                return;
            }
            LiteralCommandNode<?> node = commands.get(event.getCommandLabel());
            if (node != null) {
                event.setLiteral((LiteralCommandNode) node);
            }
        }
    }

    @Override
    public void register(LiteralCommandNode<?> node) {
        Objects.requireNonNull(node, "node");
        commands.put(node.getLiteral(), node);
    }

    @Override
    public void register(Command command, LiteralCommandNode<?> node) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(node, "node");

        Collection<String> aliases = getAliases(command);
        if (!aliases.contains(node.getLiteral())) {
            node = renameLiteralNode(node, command.getName());
        }

        for (String alias : aliases) {
            if (node.getLiteral().equals(alias)) {
                commands.put(node.getLiteral(), node);
            } else {
                LiteralCommandNode<Object> redirectNode = literal(alias)
                        .redirect((LiteralCommandNode<Object>) node)
                        .build();
                commands.put(redirectNode.getLiteral(), redirectNode);
            }
        }
    }

    static void ensureSetup() {
        // do nothing - this is only called to trigger the static initializer
    }

    static {
        try {
            Class.forName("com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent");
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Not running on modern Paper!", e);
        }
    }
}
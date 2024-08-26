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
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.command.UnknownCommandEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.brigadier.BrigadierAdapter;
import revxrsal.commands.brigadier.BrigadierConverter;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.hooks.LampCommandExecutor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static revxrsal.commands.bukkit.brigadier.BrigadierUtil.getBukkitSender;
import static revxrsal.commands.bukkit.brigadier.BrigadierUtil.renameLiteralNode;
import static revxrsal.commands.util.Strings.stripNamespace;

@SuppressWarnings({"rawtypes"})
final class ByPaperEvents<A extends BukkitCommandActor> implements BukkitBrigadierBridge<A>, BrigadierConverter<A, Object>, Listener {

    private final Map<String, LiteralCommandNode<?>> commands = new HashMap<>();
    private final String fallbackPrefix;

    private final ArgumentTypes<A> types;
    private final ActorFactory<A> actorFactory;
    private final JavaPlugin plugin;
    private boolean unknownCommandListenerRegistered = false;

    ByPaperEvents(@NotNull JavaPlugin plugin, ArgumentTypes<A> types, @NotNull ActorFactory<A> actorFactory) {
        this.plugin = plugin;
        this.fallbackPrefix = plugin.getName().toLowerCase().trim();
        this.types = types;
        this.actorFactory = actorFactory;
        registerListener(plugin);
    }

    private void registerListener(Plugin plugin) {
        // Put each listener in a class, in case one of them fails due to incompatibility.
        Bukkit.getPluginManager().registerEvents(new CommandRegisterListener(), plugin);
    }

    @Override public @NotNull ArgumentType<?> getArgumentType(@NotNull ParameterNode<A, ?> parameter) {
        return types.type(parameter);
    }

    @Override public @NotNull A createActor(@NotNull Object sender, @NotNull Lamp<A> lamp) {
        return actorFactory.create(getBukkitSender(sender), lamp);
    }

    @Override public void register(ExecutableCommand<A> command) {
        Objects.requireNonNull(command, "command");
        if (!unknownCommandListenerRegistered) {
            Bukkit.getPluginManager().registerEvents(new UnknownCommandListener(command.lamp()), plugin);
            unknownCommandListenerRegistered = true;
        }
        LiteralCommandNode<Object> node = BrigadierAdapter.createNode(command, this);
        Collection<String> aliases = BukkitBrigadierBridge.getAliases(
                plugin.getCommand(command.firstNode().name())
        );
        if (!aliases.contains(node.getLiteral())) {
            node = renameLiteralNode(node, command.firstNode().name());
        }

        for (String alias : aliases) {
            if (node.getLiteral().equals(alias)) {
                commands.put(node.getLiteral(), node);
            } else {
                LiteralCommandNode<Object> redirectNode = literal(alias)
                        .redirect(node)
                        .build();
                commands.put(redirectNode.getLiteral(), redirectNode);
            }
        }
    }

    public final class UnknownCommandListener implements Listener {
        private final Lamp<A> lamp;

        public UnknownCommandListener(Lamp<A> lamp) {
            this.lamp = lamp;
        }

        @EventHandler
        public void onUnknownCommand(UnknownCommandEvent event) {
            if (event.getCommandLine().isEmpty())
                return;
            MutableStringStream input = StringStream.createMutable(
                    stripNamespace(fallbackPrefix, event.getCommandLine())
            );
            if (commands.containsKey(input.peekUnquotedString())) {
                event.setMessage(null);
                A actor = actorFactory.create(event.getSender(), lamp);
                // This will automatically fail, we can then easily get the
                // exception message and overwrite it.
                lamp.dispatch(actor, input);
            }
        }
    }

    public final class CommandRegisterListener implements Listener {
        @EventHandler
        @SuppressWarnings("deprecation")
        public void onCommandRegistered(CommandRegisteredEvent<?> event) {
            if (!(event.getCommand() instanceof PluginCommand pCommand)) {
                return;
            }
            if (!(pCommand.getExecutor() instanceof LampCommandExecutor<?>)) {
                return;
            }
            LiteralCommandNode node = commands.get(event.getCommandLabel());
            if (node != null) {
                event.setLiteral(node);
            }
        }
    }
}
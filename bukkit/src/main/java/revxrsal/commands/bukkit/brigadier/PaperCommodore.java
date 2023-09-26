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

import com.google.common.base.Suppliers;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import revxrsal.commands.bukkit.core.BukkitCommandExecutor;
import revxrsal.commands.core.reflect.MethodCaller;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static revxrsal.commands.core.reflect.MethodCallerFactory.defaultFactory;

final class PaperCommodore extends Commodore implements Listener {

    private final Map<String, LiteralCommandNode<?>> commands = new HashMap<>();

    PaperCommodore(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        registerListener(plugin);
    }

    private void registerListener(Plugin plugin) {
        RegisterEventReflection ref = REGISTER_EVENT.get();
        Bukkit.getPluginManager().registerEvent(ref.eventClass, EMPTY_LISTENER, EventPriority.NORMAL, (listener, event) -> {
            Command command = (Command) ref.getCommand.call(event);
            if (!(command instanceof PluginCommand))
                return;
            if (!(((PluginCommand) command).getExecutor() instanceof BukkitCommandExecutor))
                return;
            String commandLabel = (String) ref.getCommandLabel.call(event);
            LiteralCommandNode<?> node = commands.get(commandLabel);
            if (node != null) {
                ref.setLiteral.call(event, node);
                ref.setRawCommand.call(event, true);
            }
        }, plugin);
    }

    // The below is kept as a reference to the above
    //
    //    @EventHandler
    //    public void onCommandRegistered(CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
    //        if (!(event.getCommand() instanceof PluginCommand command)) {
    //            return;
    //        }
    //        if (!(command.getExecutor() instanceof BukkitCommandExecutor)) {
    //            return;
    //        }
    //        LiteralCommandNode<?> node = commands.get(event.getCommandLabel());
    //        if (node != null) {
    //            event.setLiteral((LiteralCommandNode) node);
    //        }
    //    }

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

    private static final Listener EMPTY_LISTENER = new Listener() {};

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

    private static final Supplier<RegisterEventReflection> REGISTER_EVENT = Suppliers.memoize(() -> {
        try {
            Class<? extends Event> eventClass = Class
                    .forName("com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent")
                    .asSubclass(Event.class);
            Method getCommand = eventClass.getDeclaredMethod("getCommand");
            Method getCommandLabel = eventClass.getDeclaredMethod("getCommandLabel");
            Method setLiteral = eventClass.getDeclaredMethod("setLiteral", LiteralCommandNode.class);
            Method setRawCommand = eventClass.getDeclaredMethod("setRawCommand", boolean.class);
            return new RegisterEventReflection(
                    eventClass,
                    defaultFactory().createFor(getCommand),
                    defaultFactory().createFor(getCommandLabel),
                    defaultFactory().createFor(setLiteral),
                    defaultFactory().createFor(setRawCommand)
            );
        } catch (Throwable e) {
            throw new UnsupportedOperationException("Not running on modern Paper!", e);
        }
    });

    @AllArgsConstructor
    private static final class RegisterEventReflection {

        private final Class<? extends Event> eventClass;
        private final MethodCaller getCommand, getCommandLabel, setLiteral, setRawCommand;
    }


}
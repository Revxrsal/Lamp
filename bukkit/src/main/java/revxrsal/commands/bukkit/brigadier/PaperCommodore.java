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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.Plugin;
import revxrsal.commands.core.reflect.MethodCaller;
import revxrsal.commands.core.reflect.MethodCallerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class PaperCommodore extends Commodore implements Listener {

    private static final Listener EMPTY_LISTENER = new Listener() {};

    private static final Supplier<SendCommandEventReflection> ASYNC_SEND_COMMANDS_EVENT = Suppliers.memoize(() -> {
        try {
            Class<? extends Event> eventClass = Class.forName("com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent")
                    .asSubclass(Event.class);
            Method getCommandNode = eventClass.getDeclaredMethod("getCommandNode");
            Method hasFiredAsync = eventClass.getDeclaredMethod("hasFiredAsync");
            return new SendCommandEventReflection(
                    eventClass,
                    MethodCallerFactory.defaultFactory().createFor(getCommandNode),
                    MethodCallerFactory.defaultFactory().createFor(hasFiredAsync)
            );
        } catch (Throwable e) {
            throw new UnsupportedOperationException("Not running on modern Paper!", e);
        }
    });

    static {
        try {
            Class.forName("com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent");
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Not running on modern Paper!", e);
        }
    }

    private final List<CommodoreCommand> commands = new ArrayList<>();

    PaperCommodore(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        SendCommandEventReflection ref = ASYNC_SEND_COMMANDS_EVENT.get();
        Bukkit.getPluginManager().registerEvent(ref.eventClass, EMPTY_LISTENER, EventPriority.NORMAL, (listener, event) -> {
            boolean hasFiredAsync = (boolean) ref.hasFiredAsync.call(event);
            if (event.isAsynchronous() || !hasFiredAsync) {
                RootCommandNode<?> node = (RootCommandNode<?>) ref.getCommandNode.call(event);
                for (CommodoreCommand command : commands) {
                    CommandNode<?> admin = command.node.getChild("admin");
                    if (admin != null) System.out.println(admin.getRequirement());
                    command.apply(((PlayerEvent) event).getPlayer(), node);
                }
            }
        }, plugin);
    }

    @Override
    public void register(LiteralCommandNode<?> node) {
        Objects.requireNonNull(node, "node");
        commands.add(new CommodoreCommand(node, null));
    }

    @Override
    public void register(Command command, LiteralCommandNode<?> node, Predicate<? super Player> permissionTest) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(permissionTest, "permissionTest");

        Collection<String> aliases = getAliases(command);
        if (!aliases.contains(node.getLiteral())) {
            node = renameLiteralNode(node, command.getName());
        }

        for (String alias : aliases) {
            if (node.getLiteral().equals(alias)) {
                commands.add(new CommodoreCommand(node, permissionTest));
            } else {
                LiteralCommandNode<Object> redirectNode = LiteralArgumentBuilder.literal(alias)
                        .redirect((LiteralCommandNode<Object>) node)
                        .build();
                commands.add(new CommodoreCommand(redirectNode, permissionTest));
            }
        }
    }

    private static final class CommodoreCommand {

        private final LiteralCommandNode<?> node;
        private final Predicate<? super Player> permissionTest;

        private CommodoreCommand(LiteralCommandNode<?> node, Predicate<? super Player> permissionTest) {
            this.node = node;
            this.permissionTest = permissionTest;
        }

        @SuppressWarnings("rawtypes")
        public void apply(Player player, RootCommandNode<?> root) {
            if (permissionTest != null && !permissionTest.test(player)) {
                return;
            }
            removeChild(root, node.getName());
            root.addChild((CommandNode) node);
        }

        @Override
        public String toString() {
            return "CommodoreCommand[node=" + node + ", permissionTest=" + permissionTest + "]";
        }
    }

    static void ensureSetup() {
        // do nothing - this is only called to trigger the static initializer
    }

    @AllArgsConstructor
    private static final class SendCommandEventReflection {

        private final Class<? extends Event> eventClass;
        private final MethodCaller getCommandNode, hasFiredAsync;
    }
}
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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.brigadier.BrigadierConverter;
import revxrsal.commands.brigadier.BrigadierParser;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.util.BukkitVersion;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.ParameterNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static revxrsal.commands.bukkit.brigadier.BrigadierUtil.*;

final class ByReflection<A extends BukkitCommandActor> implements BukkitBrigadierBridge<A>, BrigadierConverter<A, Object> {

    // obc.CraftServer#console field
    private static final Field CONSOLE_FIELD;

    // nms.MinecraftServer#getCommandDispatcher method
    private static final Method GET_COMMAND_DISPATCHER_METHOD;

    // nms.CommandDispatcher#getDispatcher (obfuscated) method
    private static final Method GET_BRIGADIER_DISPATCHER_METHOD;

    static {
        try {
            Class<?> minecraftServer;
            Class<?> commandDispatcher;

            if (BukkitVersion.supports(1, 16)) {
                minecraftServer = BukkitVersion.findNmsClass("server.MinecraftServer");
                commandDispatcher = BukkitVersion.findNmsClass("commands.CommandDispatcher");
            } else {
                minecraftServer = BukkitVersion.findNmsClass("MinecraftServer");
                commandDispatcher = BukkitVersion.findNmsClass("CommandDispatcher");
            }

            Class<?> craftServer = BukkitVersion.findOcbClass("CraftServer");
            CONSOLE_FIELD = craftServer.getDeclaredField("console");
            CONSOLE_FIELD.setAccessible(true);

            GET_COMMAND_DISPATCHER_METHOD = Arrays.stream(minecraftServer.getDeclaredMethods())
                    .filter(method -> method.getParameterCount() == 0)
                    .filter(method -> commandDispatcher.isAssignableFrom(method.getReturnType()))
                    .findFirst().orElseThrow(NoSuchMethodException::new);
            GET_COMMAND_DISPATCHER_METHOD.setAccessible(true);

            GET_BRIGADIER_DISPATCHER_METHOD = Arrays.stream(commandDispatcher.getDeclaredMethods())
                    .filter(method -> method.getParameterCount() == 0)
                    .filter(method -> CommandDispatcher.class.isAssignableFrom(method.getReturnType()))
                    .findFirst().orElseThrow(NoSuchMethodException::new);
            GET_BRIGADIER_DISPATCHER_METHOD.setAccessible(true);

        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final JavaPlugin plugin;
    private final ArgumentTypes<A> types;
    private final ActorFactory<A> factory;
    private final BrigadierParser<Object, A> parser = new BrigadierParser<>(this);

    private final RootCommandNode<Object> registeredNodes = new RootCommandNode<>();

    ByReflection(JavaPlugin plugin, ArgumentTypes<A> types, ActorFactory<A> factory) {
        this.plugin = plugin;
        this.types = types;
        this.factory = factory;
        this.plugin.getServer().getPluginManager().registerEvents(new ServerReloadListener(), this.plugin);
    }

    private CommandDispatcher<?> getDispatcher() {
        try {
            Object mcServerObject = CONSOLE_FIELD.get(Bukkit.getServer());
            Object commandDispatcherObject = GET_COMMAND_DISPATCHER_METHOD.invoke(mcServerObject);
            return (CommandDispatcher<?>) GET_BRIGADIER_DISPATCHER_METHOD.invoke(commandDispatcherObject);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void register(LiteralCommandNode<Object> node) {
        Objects.requireNonNull(node, "node");

        CommandDispatcher dispatcher = getDispatcher();
        RootCommandNode root = dispatcher.getRoot();

        BrigadierUtil.removeChild(root, node.getName());
        root.addChild(node);
        registeredNodes.addChild(node);
    }

    @Override public void register(ExecutableCommand<A> command) {
        Objects.requireNonNull(command, "command");
        LiteralCommandNode<Object> node = parser.createNode(command);

        PluginCommand bCommand = plugin.getCommand(command.firstNode().name());
        Collection<String> aliases = BukkitBrigadierBridge.getAliases(bCommand);
        if (!aliases.contains(node.getLiteral())) {
            node = renameLiteralNode(node, command.firstNode().name());
        }

        for (String alias : aliases) {
            if (node.getLiteral().equals(alias)) {
                register(node);
            } else {
                register(LiteralArgumentBuilder.literal(alias).redirect(node).build());
            }
        }
        plugin.getServer().getPluginManager().registerEvents(new CommandDataSendListener(bCommand), plugin);
    }

    @Override public @NotNull ArgumentType<?> getArgumentType(@NotNull ParameterNode<A, ?> parameter) {
        return types.type(parameter);
    }

    @Override public @NotNull A createActor(@NotNull Object sender, @NotNull Lamp<A> lamp) {
        return factory.create(getBukkitSender(sender), lamp);
    }

    /**
     * Removes minecraft namespaced argument data, & data for players without permission to view the
     * corresponding commands.
     */
    private static final class CommandDataSendListener implements Listener {

        private final Set<String> minecraftPrefixedAliases;

        CommandDataSendListener(Command pluginCommand) {
            minecraftPrefixedAliases = BukkitBrigadierBridge.getAliases(pluginCommand).stream()
                    .map(alias -> "minecraft:" + alias).collect(Collectors.toSet());
        }

        @EventHandler
        public void onCommandSend(PlayerCommandSendEvent e) {
            // always remove 'minecraft:' prefixed aliases added by craftbukkit.
            // this happens because bukkit thinks our injected commands are vanilla commands.
            e.getCommands().removeAll(minecraftPrefixedAliases);
        }
    }

    /**
     * Listens for server (re)loads, and re-adds all registered nodes to the dispatcher.
     */
    private final class ServerReloadListener implements Listener {

        @SuppressWarnings({"rawtypes", "unchecked"})
        @EventHandler
        public void onLoad(ServerLoadEvent e) {
            CommandDispatcher dispatcher = getDispatcher();
            RootCommandNode root = dispatcher.getRoot();

            for (CommandNode<?> node : registeredNodes.getChildren()) {
                removeChild(root, node.getName());
                root.addChild(node);
            }
        }

        @EventHandler
        @SuppressWarnings("rawtypes")
        public void onPluginDisable(PluginDisableEvent e) {
            if (plugin != e.getPlugin()) return;
            CommandDispatcher dispatcher = getDispatcher();
            RootCommandNode root = dispatcher.getRoot();

            for (CommandNode<?> node : registeredNodes.getChildren()) {
                removeChild(root, node.getName());
            }
        }
    }
}
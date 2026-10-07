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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.brigadier.BrigadierConverter;
import revxrsal.commands.brigadier.BrigadierParser;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.FallbackPrefix;
import revxrsal.commands.bukkit.util.BukkitVersion;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.ParameterNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static revxrsal.commands.bukkit.brigadier.BrigadierUtil.getBukkitSender;
import static revxrsal.commands.bukkit.brigadier.BrigadierUtil.removeChild;

/**
 * A Brigadier bridge for Spigot 1.19+ (non-Paper), where injecting nodes into the
 * dispatcher that is sent to clients no longer works, because Spigot rebuilds that
 * dispatcher from the {@code CommandMap} in {@code CraftServer#syncCommands()}, turning
 * every Bukkit command into a greedy {@code BukkitCommandWrapper}.
 * <p>
 * Instead, this registers commands the way vanilla commands are registered:
 * <ol>
 *     <li>The Brigadier node goes into {@code MinecraftServer#vanillaCommandDispatcher}</li>
 *     <li>The {@code CommandMap} entry for the command is a CraftBukkit
 *     {@code VanillaCommandWrapper}, which {@code syncCommands()} copies verbatim
 *     into the client dispatcher, and which executes through Brigadier</li>
 *     <li>After the server has loaded, the {@code minecraft:} namespaced copies that
 *     Bukkit creates for vanilla commands are removed</li>
 * </ol>
 * This mirrors CommandAPI's {@code SpigotCommandRegistration}, using reflection so it
 * needs no version-specific code.
 *
 * @param <A> The actor type
 */
final class BySpigotVanillaWrapper<A extends BukkitCommandActor> implements BukkitBrigadierBridge<A>, BrigadierConverter<A, Object> {

    // obc.CraftServer#console
    private static final Field CONSOLE_FIELD;

    // nms.MinecraftServer#vanillaCommandDispatcher (added by CraftBukkit, never obfuscated)
    private static final Field VANILLA_DISPATCHER_FIELD;

    // nms.MinecraftServer#getCommands(), the dispatcher sent to clients (obfuscated before 26.1)
    private static final Method GET_RESOURCES_COMMANDS_METHOD;

    // nms.Commands#getDispatcher() (obfuscated before 26.1)
    private static final Method GET_BRIGADIER_DISPATCHER_METHOD;

    // obc.command.VanillaCommandWrapper(nms.Commands, CommandNode)
    private static final Constructor<?> VANILLA_WRAPPER_CONSTRUCTOR;

    // SimpleCommandMap#knownCommands
    private static final Field KNOWN_COMMANDS_FIELD;

    static {
        try {
            Class<?> minecraftServer = BukkitVersion.findNmsClass("server.MinecraftServer", "MinecraftServer");
            Class<?> commands = BukkitVersion.findNmsClass("commands.Commands", "commands.CommandDispatcher", "CommandDispatcher");
            Class<?> craftServer = BukkitVersion.findOcbClass("CraftServer");

            CONSOLE_FIELD = craftServer.getDeclaredField("console");
            CONSOLE_FIELD.setAccessible(true);

            VANILLA_DISPATCHER_FIELD = minecraftServer.getField("vanillaCommandDispatcher");

            GET_RESOURCES_COMMANDS_METHOD = Arrays.stream(minecraftServer.getDeclaredMethods())
                    .filter(method -> method.getParameterCount() == 0)
                    .filter(method -> commands.isAssignableFrom(method.getReturnType()))
                    .findFirst().orElseThrow(NoSuchMethodException::new);
            GET_RESOURCES_COMMANDS_METHOD.setAccessible(true);

            GET_BRIGADIER_DISPATCHER_METHOD = Arrays.stream(commands.getDeclaredMethods())
                    .filter(method -> method.getParameterCount() == 0)
                    .filter(method -> CommandDispatcher.class.isAssignableFrom(method.getReturnType()))
                    .findFirst().orElseThrow(NoSuchMethodException::new);
            GET_BRIGADIER_DISPATCHER_METHOD.setAccessible(true);

            Class<?> vanillaWrapper = BukkitVersion.findOcbClass("command.VanillaCommandWrapper");
            VANILLA_WRAPPER_CONSTRUCTOR = vanillaWrapper.getConstructor(commands, CommandNode.class);

            KNOWN_COMMANDS_FIELD = SimpleCommandMap.class.getDeclaredField("knownCommands");
            KNOWN_COMMANDS_FIELD.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final JavaPlugin plugin;
    private final ArgumentTypes<A> types;
    private final ActorFactory<A> factory;
    private final String defaultFallbackPrefix;
    private final BrigadierParser<Object, A> parser = new BrigadierParser<>(this);

    /**
     * All root nodes we registered. Commands sharing a root (e.g. {@code /foo a} and
     * {@code /foo b}) are merged into a single node here.
     */
    private final RootCommandNode<Object> registeredNodes = new RootCommandNode<>();

    BySpigotVanillaWrapper(JavaPlugin plugin, ArgumentTypes<A> types, ActorFactory<A> factory, String defaultFallbackPrefix) {
        this.plugin = plugin;
        this.types = types;
        this.factory = factory;
        this.defaultFallbackPrefix = defaultFallbackPrefix;
        plugin.getServer().getPluginManager().registerEvents(new Listeners(), plugin);
    }

    @Override public void register(ExecutableCommand<A> command) {
        LiteralCommandNode<Object> node = parser.createNode(command);
        String label = node.getLiteral();
        String fallbackPrefix = command.annotations()
                .mapOr(FallbackPrefix.class, FallbackPrefix::value, defaultFallbackPrefix)
                .toLowerCase().trim();

        boolean isNewRoot = registeredNodes.getChild(label) == null;
        // merges into an existing root node if there is one, so earlier commands are kept
        BrigadierParser.addChild(registeredNodes, node);
        @SuppressWarnings("unchecked")
        LiteralCommandNode<Object> merged = (LiteralCommandNode<Object>) registeredNodes.getChild(label);
        if (!isNewRoot)
            return; // the merged node is already wired everywhere and was updated in place

        RootCommandNode<Object> vanillaRoot = vanillaDispatcher().getRoot();
        removeChild(vanillaRoot, label);
        BrigadierParser.addChild(vanillaRoot, merged);

        Command wrapper = createVanillaWrapper(merged);
        Map<String, Command> knownCommands = knownCommands();
        knownCommands.put(label, wrapper);
        knownCommands.put(fallbackPrefix + ":" + label, wrapper);

        // Registered after the server started: syncCommands() won't run again on its
        // own, so put the node into the client dispatcher and re-send it ourselves.
        RootCommandNode<Object> resourcesRoot = resourcesDispatcher().getRoot();
        removeChild(resourcesRoot, label);
        BrigadierParser.addChild(resourcesRoot, merged);
        resendCommands();
    }

    @Override public @NotNull ArgumentType<?> getArgumentType(@NotNull ParameterNode<A, ?> parameter) {
        return types.type(parameter);
    }

    @Override public @NotNull A createActor(@NotNull Object sender, @NotNull Lamp<A> lamp) {
        return factory.create(getBukkitSender(sender), lamp);
    }

    private @NotNull Command createVanillaWrapper(CommandNode<Object> node) {
        try {
            Command wrapper = (Command) VANILLA_WRAPPER_CONSTRUCTOR.newInstance(vanillaCommands(), node);
            // the wrapper defaults to 'minecraft.command.<name>', which nobody has.
            // Lamp checks permissions itself through the node requirements.
            wrapper.setPermission(null);
            return wrapper;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void resendCommands() {
        if (Bukkit.getOnlinePlayers().isEmpty())
            return;
        // updateCommands() rebuilds from the client dispatcher, so do it once all
        // commands registered in this tick are in place
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers())
                player.updateCommands();
        });
    }

    private void removeMinecraftNamespaces() {
        Map<String, Command> knownCommands = knownCommands();
        RootCommandNode<Object> resourcesRoot = resourcesDispatcher().getRoot();
        for (CommandNode<Object> node : registeredNodes.getChildren()) {
            String namespaced = "minecraft:" + node.getName();
            knownCommands.remove(namespaced);
            removeChild(resourcesRoot, namespaced);
        }
    }

    private static Object minecraftServer() throws ReflectiveOperationException {
        return CONSOLE_FIELD.get(Bukkit.getServer());
    }

    private static Object vanillaCommands() throws ReflectiveOperationException {
        return VANILLA_DISPATCHER_FIELD.get(minecraftServer());
    }

    @SuppressWarnings("unchecked")
    private static CommandDispatcher<Object> vanillaDispatcher() {
        try {
            return (CommandDispatcher<Object>) GET_BRIGADIER_DISPATCHER_METHOD.invoke(vanillaCommands());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static CommandDispatcher<Object> resourcesDispatcher() {
        try {
            Object commands = GET_RESOURCES_COMMANDS_METHOD.invoke(minecraftServer());
            return (CommandDispatcher<Object>) GET_BRIGADIER_DISPATCHER_METHOD.invoke(commands);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Command> knownCommands() {
        try {
            Object commandMap = Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());
            return (Map<String, Command>) KNOWN_COMMANDS_FIELD.get(commandMap);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final class Listeners implements Listener {

        /**
         * Fired after {@code CraftServer#enablePlugins(POSTWORLD)}, by which point
         * {@code setVanillaCommands()} has added {@code minecraft:<label>} copies of
         * our commands and {@code syncCommands()} has built the client dispatcher.
         */
        @EventHandler
        public void onServerLoad(ServerLoadEvent event) {
            removeMinecraftNamespaces();
            resendCommands();
        }

        @EventHandler
        public void onCommandSend(PlayerCommandSendEvent event) {
            // in case anything re-adds them later (e.g. /minecraft:reload)
            for (CommandNode<Object> node : registeredNodes.getChildren())
                event.getCommands().remove("minecraft:" + node.getName());
        }
    }
}

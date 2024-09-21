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
package revxrsal.commands.bukkit.util;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import static revxrsal.commands.util.Preconditions.cannotInstantiate;

/**
 * A utility class for constructing {@link PluginCommand} objects at runtime
 * (instead of requiring users to register them in {@code plugin.yml}s)
 */
@ApiStatus.Internal
public final class PluginCommands {

    private static final Constructor<PluginCommand> COMMAND_CONSTRUCTOR;
    private static final @Nullable Field KNOWN_COMMANDS;
    private static final CommandMap COMMAND_MAP;

    static {
        Constructor<PluginCommand> ctr;
        Field knownCommands = null;
        CommandMap commandMap;
        try {
            ctr = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            ctr.setAccessible(true);
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            if (commandMap instanceof SimpleCommandMap) {
                knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommands.setAccessible(true);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalStateException("Unable to access PluginCommand(String, Plugin) construtor!");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalStateException("Unable to access Bukkit.getServer()#commandMap!");
        }
        COMMAND_CONSTRUCTOR = ctr;
        COMMAND_MAP = commandMap;
        KNOWN_COMMANDS = knownCommands;
    }

    private PluginCommands() {
        cannotInstantiate(PluginCommands.class);
    }

    @SneakyThrows
    @CheckReturnValue
    public static @NotNull PluginCommand create(String name, @NotNull JavaPlugin plugin) {
        return create(plugin.getName(), name, plugin);
    }

    @SneakyThrows
    @CheckReturnValue
    public static @NotNull PluginCommand create(String fallbackPrefix, String name, @NotNull JavaPlugin plugin) {
        PluginCommand command = plugin.getCommand(name);
        if (command != null)
            return command;
        command = COMMAND_CONSTRUCTOR.newInstance(name, plugin);
        COMMAND_MAP.register(fallbackPrefix, command);
        return command;
    }

    public static void unregister(@NotNull PluginCommand command, @NotNull JavaPlugin owningPlugin) {
        command.unregister(COMMAND_MAP);
        Map<String, Command> knownCommands = getKnownCommands();
        if (knownCommands != null) {
            Command rawAlias = knownCommands.get(command.getName());
            if (rawAlias instanceof PluginCommand && ((PluginCommand) rawAlias).getPlugin() == owningPlugin)
                knownCommands.remove(command.getName());
        }
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows private static @Nullable Map<String, Command> getKnownCommands() {
        if (KNOWN_COMMANDS != null)
            return (Map<String, Command>) KNOWN_COMMANDS.get(COMMAND_MAP);
        return null;
    }
}

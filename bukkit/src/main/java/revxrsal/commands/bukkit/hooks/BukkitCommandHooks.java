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
package revxrsal.commands.bukkit.hooks;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.util.PluginCommands;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.hook.CancelHandle;
import revxrsal.commands.hook.CommandRegisteredHook;
import revxrsal.commands.hook.CommandUnregisteredHook;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class BukkitCommandHooks implements CommandRegisteredHook<BukkitCommandActor>, CommandUnregisteredHook<BukkitCommandActor> {

    private final Set<String> registeredRootNames = new HashSet<>();

    private final JavaPlugin plugin;
    private final ActorFactory<?> actorFactory;

    public BukkitCommandHooks(JavaPlugin plugin, ActorFactory<?> actorFactory) {
        this.plugin = plugin;
        this.actorFactory = actorFactory;
    }

    @Override
    public void onRegistered(@NotNull ExecutableCommand<BukkitCommandActor> command, @NotNull CancelHandle cancelHandle) {
        String name = command.firstNode().name();
        if (registeredRootNames.add(name)) {
            // command wasn't registered before. register it.

            PluginCommand cmd = PluginCommands.create(command.firstNode().name(), plugin);

            LampCommandExecutor<BukkitCommandActor> executor = new LampCommandExecutor<>(command.lamp(), ((ActorFactory) actorFactory));
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);

            if (cmd.getDescription().isEmpty() && command.description() != null)
                cmd.setDescription(Objects.requireNonNull(command.description()));
            if (cmd.getUsage().isEmpty())
                cmd.setUsage(command.usage());
        }
    }

    @Override
    public void onUnregistered(@NotNull ExecutableCommand<BukkitCommandActor> command, @NotNull CancelHandle cancelHandle) {
        String name = command.firstNode().name();
        PluginCommand cmd = plugin.getCommand(name);
        if (cmd == null)
            return;
        PluginCommands.unregister(cmd, plugin);
    }
}

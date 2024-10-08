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
package revxrsal.commands.bungee.hooks;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.bungee.actor.ActorFactory;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.hook.CancelHandle;
import revxrsal.commands.hook.CommandRegisteredHook;

import java.util.HashSet;
import java.util.Set;

public final class BungeeCommandHooks implements CommandRegisteredHook<BungeeCommandActor> {

    private final Set<String> registeredRootNames = new HashSet<>();

    private final Plugin plugin;
    private final ActorFactory<?> actorFactory;

    public BungeeCommandHooks(Plugin plugin, ActorFactory<?> actorFactory) {
        this.plugin = plugin;
        this.actorFactory = actorFactory;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void onRegistered(@NotNull ExecutableCommand<BungeeCommandActor> command, @NotNull CancelHandle cancelHandle) {
        String name = command.firstNode().name();
        if (registeredRootNames.add(name)) {
            Lamp<BungeeCommandActor> lamp = command.lamp();
            BungeeCommand<BungeeCommandActor> bungeeCommand = new BungeeCommand<>(name, (Lamp) lamp, actorFactory);
            ProxyServer.getInstance().getPluginManager().registerCommand(plugin, bungeeCommand);
        }
    }
}

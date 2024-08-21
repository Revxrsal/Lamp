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
package revxrsal.commands.sponge.hooks;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.hook.CancelHandle;
import revxrsal.commands.hook.CommandRegisteredHook;
import revxrsal.commands.sponge.actor.ActorFactory;
import revxrsal.commands.sponge.actor.SpongeCommandActor;

import java.util.HashMap;
import java.util.Map;

public final class SpongeCommandHooks<A extends SpongeCommandActor> implements CommandRegisteredHook<A> {

    private final Map<String, SpongeCommand<A>> registered = new HashMap<>();

    private final Object plugin;
    private final ActorFactory<A> actorFactory;

    public SpongeCommandHooks(Object plugin, ActorFactory<A> actorFactory) {
        this.plugin = plugin;
        this.actorFactory = actorFactory;
        Sponge.eventManager().registerListeners((PluginContainer) plugin, this);
    }

    @Override public void onRegistered(@NotNull ExecutableCommand<A> command, @NotNull CancelHandle cancelHandle) {
        String name = command.firstNode().name();
        if (!registered.containsKey(name)) {
            SpongeCommand<A> spongeCommand = new SpongeCommand<>(name, command.lamp(), actorFactory, command.permission());
            registered.put(name, spongeCommand);
        }
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Raw> event) {
        registered.forEach((name, command) -> event.register((PluginContainer) plugin, command, name));
    }
}

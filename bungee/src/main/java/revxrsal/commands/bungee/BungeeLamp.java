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
package revxrsal.commands.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.bungee.actor.ActorFactory;
import revxrsal.commands.bungee.actor.BungeeCommandActor;

import static revxrsal.commands.bungee.BungeeVisitors.*;

/**
 * Creates {@link Lamp} instances that contain relevant registrations
 * for the Bungee platform.
 */
public final class BungeeLamp {

    /**
     * Returns a {@link Lamp.Builder} that contains the default registrations
     * for the Bungee platform
     *
     * @param plugin       The plugin instance
     * @param actorFactory The actor factory for creating custom implementations of {@link BungeeCommandActor}
     * @param <A>          The actor type
     * @return A {@link Lamp.Builder}
     */
    public static <A extends BungeeCommandActor> Lamp.Builder<A> builder(
            @NotNull Plugin plugin,
            @NotNull ActorFactory<A> actorFactory
    ) {
        Lamp.Builder<A> builder = Lamp.builder();
        return builder
                .accept(legacyColorCodes())
                .accept(bungeeSenderResolver())
                .accept(bungeeParameterTypes())
                .accept(bungeeExceptionHandler())
                .accept(bungeePermissions())
                .accept(registrationHooks(plugin, actorFactory))
                .accept(pluginContextParameters(plugin));
    }

    /**
     * Returns a {@link Lamp.Builder} that contains the default registrations
     * for the Bungee platform
     *
     * @param plugin The plugin instance
     * @return A {@link Lamp.Builder}
     */
    public static Lamp.Builder<BungeeCommandActor> builder(@NotNull Plugin plugin) {
        return builder(plugin, ActorFactory.defaultFactory());
    }
}

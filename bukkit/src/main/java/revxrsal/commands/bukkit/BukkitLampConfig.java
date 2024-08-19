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
package revxrsal.commands.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.brigadier.BukkitArgumentTypes;

import java.util.function.Consumer;

import static revxrsal.commands.bukkit.BukkitVisitors.*;
import static revxrsal.commands.util.Preconditions.notNull;

/**
 * A collective object that contains all Bukkit-only properties and allows for
 * easy customizing and chaining using a builder
 *
 * @param <A> The actor type.
 */
public final class BukkitLampConfig<A extends BukkitCommandActor> implements LampBuilderVisitor<A> {

    private final ActorFactory<A> actorFactory;
    private final ArgumentTypes<A> argumentTypes;
    private final JavaPlugin plugin;

    private BukkitLampConfig(ActorFactory<A> actorFactory, ArgumentTypes<A> argumentTypes, JavaPlugin plugin) {
        this.actorFactory = actorFactory;
        this.argumentTypes = argumentTypes;
        this.plugin = plugin;
    }

    public static <A extends BukkitCommandActor> Builder<A> builder(@NotNull JavaPlugin plugin) {
        notNull(plugin, "plugin");
        return new Builder<>(plugin);
    }

    public static BukkitLampConfig<BukkitCommandActor> createDefault(@NotNull JavaPlugin plugin) {
        notNull(plugin, "plugin");
        return new BukkitLampConfig<>(ActorFactory.defaultFactory(), BukkitArgumentTypes.<BukkitCommandActor>builder().build(), plugin);
    }

    @Override public void visit(Lamp.@NotNull Builder<A> builder) {
        builder.accept(legacyColorCodes())
                .accept(bukkitSenderResolver())
                .accept(bukkitParameterTypes())
                .accept(bukkitExceptionHandler())
                .accept(bukkitPermissions())
                .accept(registrationHooks(plugin))
                .accept(brigadier(plugin, argumentTypes, actorFactory))
                .accept(pluginContextParameters(plugin));
    }

    public static class Builder<A extends BukkitCommandActor> {

        private ActorFactory<A> actorFactory;
        private final ArgumentTypes.Builder<A> argumentTypes = BukkitArgumentTypes.builder();
        private final @NotNull JavaPlugin plugin;

        Builder(@NotNull JavaPlugin plugin) {
            this.plugin = plugin;
        }

        public Builder<A> actorFactory(ActorFactory<A> actorFactory) {
            this.actorFactory = actorFactory;
            return this;
        }

        public ArgumentTypes.Builder<A> argumentTypes() {
            return argumentTypes;
        }

        public Builder<A> argumentTypes(Consumer<ArgumentTypes.Builder<A>> consumer) {
            consumer.accept(argumentTypes);
            return this;
        }

        public BukkitLampConfig<A> build() {
            return new BukkitLampConfig<>(this.actorFactory, this.argumentTypes.build(), this.plugin);
        }
    }
}

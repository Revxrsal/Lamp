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
package revxrsal.commands.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.velocity.actor.ActorFactory;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

import java.util.function.Consumer;

import static revxrsal.commands.util.Preconditions.notNull;
import static revxrsal.commands.velocity.VelocityVisitors.*;

/**
 * A collective object that contains all Velocity-only properties and allows for
 * easy customizing and chaining using a builder
 *
 * @param <A> The actor type.
 */
public final class VelocityLampConfig<A extends VelocityCommandActor> implements LampBuilderVisitor<A> {

    private final ActorFactory<A> actorFactory;
    private final ArgumentTypes<A> argumentTypes;
    private final Object plugin;
    private final ProxyServer server;

    private VelocityLampConfig(ActorFactory<A> actorFactory, ArgumentTypes<A> argumentTypes, Object plugin, ProxyServer server) {
        this.actorFactory = actorFactory;
        this.argumentTypes = argumentTypes;
        this.plugin = plugin;
        this.server = server;
    }

    public static <A extends VelocityCommandActor> Builder<A> builder(@NotNull Object plugin, @NotNull ProxyServer server) {
        notNull(plugin, "plugin");
        notNull(server, "proxy server");
        return new Builder<>(plugin, server);
    }

    public static VelocityLampConfig<VelocityCommandActor> createDefault(@NotNull Object plugin, @NotNull ProxyServer server) {
        notNull(plugin, "plugin");
        return new VelocityLampConfig<>(
                ActorFactory.defaultFactory(),
                ArgumentTypes.<VelocityCommandActor>builder().build(),
                plugin,
                server
        );
    }

    @Override public void visit(Lamp.@NotNull Builder<A> builder) {
        builder
                .accept(legacyColorCodes())
                .accept(velocitySenderResolver())
                .accept(velocityParameterTypes(server))
                .accept(velocityExceptionHandler())
                .accept(velocityPermissions())
                .accept(registrationHooks(this))
                .accept(pluginContextParameters(plugin));
    }

    public ActorFactory<A> actorFactory() {
        return actorFactory;
    }

    public ArgumentTypes<A> argumentTypes() {
        return argumentTypes;
    }

    public Object plugin() {
        return plugin;
    }

    public ProxyServer server() {
        return server;
    }

    public static class Builder<A extends VelocityCommandActor> {

        private ActorFactory<A> actorFactory;
        private final ArgumentTypes.Builder<A> argumentTypes = ArgumentTypes.builder();
        private final @NotNull Object plugin;
        private final @NotNull ProxyServer server;

        Builder(@NotNull Object plugin, @NotNull ProxyServer server) {
            this.plugin = plugin;
            this.server = server;
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

        public VelocityLampConfig<A> build() {
            return new VelocityLampConfig<>(actorFactory, argumentTypes.build(), plugin, server);
        }
    }
}

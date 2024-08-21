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
package revxrsal.commands.velocity.hooks;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.brigadier.BrigadierAdapter;
import revxrsal.commands.brigadier.BrigadierConverter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.hook.CancelHandle;
import revxrsal.commands.hook.CommandRegisteredHook;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.velocity.VelocityLampConfig;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

import java.util.HashSet;
import java.util.Set;

/**
 * A hook that registers Lamp commands into Velocity
 *
 * @param <A> The actor type
 */
public final class VelocityCommandHooks<A extends VelocityCommandActor> implements CommandRegisteredHook<A>, BrigadierConverter<A, CommandSource> {

    /**
     * Tracks the commands we registered
     */
    private final Set<String> registeredRootNames = new HashSet<>();

    private final VelocityLampConfig<A> config;

    public VelocityCommandHooks(VelocityLampConfig<A> config) {
        this.config = config;
    }

    @Override
    public void onRegistered(@NotNull ExecutableCommand<A> command, @NotNull CancelHandle cancelHandle) {
        String name = command.firstNode().name();
        if (registeredRootNames.add(name)) {
            // command wasn't registered before. register it.
            LiteralCommandNode<CommandSource> node = BrigadierAdapter.createNode(command, this);
            BrigadierCommand brigadierCommand = new BrigadierCommand(node);
            config.server().getCommandManager().register(brigadierCommand);
        }
    }

    @Override
    public @NotNull ArgumentType<?> getArgumentType(@NotNull ParameterNode<A, ?> parameter) {
        return config.argumentTypes().type(parameter);
    }

    @Override
    public @NotNull A createActor(@NotNull CommandSource sender, @NotNull Lamp<A> lamp) {
        return config.actorFactory().create(sender, lamp);
    }
}

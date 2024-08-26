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

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.sponge.actor.ActorFactory;
import revxrsal.commands.sponge.actor.SpongeCommandActor;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static revxrsal.commands.util.Collections.map;
import static revxrsal.commands.util.Strings.stripNamespace;

public class SpongeCommand<A extends SpongeCommandActor> implements Command.Raw {

    private final String name;
    private final Lamp<A> lamp;
    private final @NotNull ActorFactory<A> actorFactory;
    private final CommandPermission<A> permission;

    public SpongeCommand(String name, Lamp<A> lamp, @NotNull ActorFactory<A> actorFactory, CommandPermission<A> permission) {
        this.name = name;
        this.lamp = lamp;
        this.actorFactory = actorFactory;
        this.permission = permission;
    }

    private static @NotNull MutableStringStream createInput(String commandName, ArgumentReader args) {
        StringJoiner userInput = new StringJoiner(" ");
        userInput.add(stripNamespace(commandName));
        if (args.totalLength() != 0)
            userInput.add(args.input());
        return StringStream.createMutable(userInput.toString());
    }

    @Override
    public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
        return CommandResult.success();
    }

    @Override
    public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
        A actor = actorFactory.create(cause, lamp);
        MutableStringStream input = createInput(name, arguments);
        List<String> completions = lamp.autoCompleter().complete(actor, input);
        // on older versions, we get funny behavior when suggestions contain spaces
        return map(completions, CommandCompletion::of);
    }

    @Override public boolean canExecute(CommandCause cause) {
        if (permission.equals(CommandPermission.alwaysTrue()))
            return true;
        A actor = actorFactory.create(cause, lamp);
        return permission.isExecutableBy(actor);
    }

    @Override public Optional<Component> shortDescription(CommandCause cause) {
        return Optional.empty();
    }

    @Override public Optional<Component> extendedDescription(CommandCause cause) {
        return Optional.empty();
    }

    @Override public Component usage(CommandCause cause) {
        return null;
    }
}

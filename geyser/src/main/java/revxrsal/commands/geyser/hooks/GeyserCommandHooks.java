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
package revxrsal.commands.geyser.hooks;

import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.entity.type.player.GeyserPlayerEntity;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.geyser.actor.ActorFactory;
import revxrsal.commands.geyser.actor.GeyserCommandActor;
import revxrsal.commands.geyser.annotation.BedrockOnly;
import revxrsal.commands.geyser.annotation.PlayerOnly;
import revxrsal.commands.geyser.sender.GeyserCommandPermission;
import revxrsal.commands.hook.CancelHandle;
import revxrsal.commands.hook.CommandRegisteredHook;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Collects Lamp commands by their root name, and registers each root
 * into Geyser once {@link GeyserDefineCommandsEvent} is fired.
 * <p>
 * Because Geyser only accepts commands during that event, all commands must
 * be registered into Lamp before it fires (i.e. in the extension's
 * pre-initialize stage).
 */
public final class GeyserCommandHooks<A extends GeyserCommandActor> implements CommandRegisteredHook<A> {

    private final Map<String, List<ExecutableCommand<A>>> rootCommands = new LinkedHashMap<>();
    private final Extension extension;
    private final ActorFactory<A> actorFactory;
    private boolean commandsDefined = false;

    public GeyserCommandHooks(@NotNull Extension extension, @NotNull ActorFactory<A> actorFactory) {
        this.extension = extension;
        this.actorFactory = actorFactory;
        extension.eventBus().subscribe(GeyserDefineCommandsEvent.class, this::onDefineCommands);
    }

    @Override
    public void onRegistered(@NotNull ExecutableCommand<A> command, @NotNull CancelHandle cancelHandle) {
        if (commandsDefined)
            throw new IllegalStateException("Cannot register command '" + command.path() + "': Geyser commands have " +
                    "already been defined. Register your commands before GeyserDefineCommandsEvent is fired.");
        rootCommands.computeIfAbsent(command.firstNode().name(), name -> new ArrayList<>())
                .add(command);
    }

    private void onDefineCommands(@NotNull GeyserDefineCommandsEvent event) {
        commandsDefined = true;
        rootCommands.forEach((name, commands) -> event.register(createCommand(name, commands)));
    }

    private @NotNull Command createCommand(@NotNull String name, @NotNull List<ExecutableCommand<A>> commands) {
        Command.Builder<CommandSource> builder = Command.builder(extension)
                .name(name);
        commands.stream()
                .map(ExecutableCommand::description)
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(builder::description);
        // Geyser only accepts a single permission node per root. When subcommands
        // disagree, leave it unset and let Lamp check each command on dispatch.
        String permission = sharedPermission(commands);
        if (permission != null) {
            builder.permission(permission);
        }
        if (commands.stream().allMatch(this::isBedrockOnly)) {
            builder.bedrockOnly(true);
        }
        if (commands.stream().allMatch(command -> command.annotations().contains(PlayerOnly.class))) {
            builder.playerOnly(true);
        }
        Lamp<A> lamp = commands.getFirst().lamp();
        builder.executor((source, geyserCommand, args) -> {
            StringJoiner userInput = new StringJoiner(" ");
            userInput.add(geyserCommand.name());
            for (String arg : args) userInput.add(arg);
            A actor = actorFactory.create(source, lamp);
            lamp.dispatch(actor, userInput.toString());
        });
        return builder.build();
    }

    private @Nullable String sharedPermission(@NotNull List<ExecutableCommand<A>> commands) {
        List<String> permissions = commands.stream()
                .map(command -> command.permission() instanceof GeyserCommandPermission(String permission) ? permission : null)
                .distinct()
                .toList();
        return permissions.size() == 1 ? permissions.getFirst() : null;
    }

    private boolean isBedrockOnly(@NotNull ExecutableCommand<A> command) {
        if (command.annotations().contains(BedrockOnly.class)) {
            return true;
        }
        var parameters = command.parameters();
        if (parameters.isEmpty()) {
            return false;
        }
        var firstParam = parameters.entrySet().iterator().next().getValue();
        return firstParam.type().equals(GeyserConnection.class) || firstParam.type().equals(GeyserPlayerEntity.class);
    }
}

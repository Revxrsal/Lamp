package revxrsal.commands.sponge.core;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.exception.ArgumentParseException;
import revxrsal.commands.sponge.SpongeCommandHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// i'm not sure if we are supposed to be providing implementations
// for testPermission(), getHelp() and getUsage().
public final class SpongeCommandRaw implements Command.Raw {

    private final SpongeCommandHandler handler;
    private final CommandPermission permission;
    private final String name;

    public SpongeCommandRaw(SpongeCommandHandler handler, String name, @NotNull CommandPermission permission) {
        this.handler = handler;
        this.name = name;
        this.permission = permission;
    }

    @Override
    public CommandResult process(CommandCause cause, ArgumentReader.Mutable args) throws CommandException {
        CommandActor actor = new SpongeActor(cause, handler);
        try {
            ArgumentStack arguments = ArgumentStack.parse(args.input());
            arguments.addFirst(name);
            handler.dispatch(actor, arguments);
        } catch (Throwable t) {
            handler.getExceptionHandler().handleException(t, actor);
        }
        return CommandResult.success();
    }

    @Override
    public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {

        final List<CommandCompletion> complete = new ArrayList<>();
        try {
            final CommandActor actor = new SpongeActor(cause, handler);
            final ArgumentStack args = ArgumentStack.parseForAutoCompletion(arguments.input());
            args.addFirst(name);
            handler.getAutoCompleter().complete(actor, args).forEach((entry)-> complete.add(CommandCompletion.of(entry)));
            return complete;
        } catch (ArgumentParseException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean canExecute(CommandCause cause) {
        final CommandActor actor = new SpongeActor(cause, handler);
        return permission.canExecute(actor);
    }

    @Override
    public Optional<Component> shortDescription(CommandCause cause) {
        return Optional.empty();
    }

    @Override
    public Optional<Component> extendedDescription(CommandCause cause) {
        return Optional.empty();
    }

    @Override
    public Component usage(CommandCause cause) {
        return Component.text("");
    }
}

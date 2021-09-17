package revxrsal.commands.sponge.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.sponge.SpongeCommandHandler;

import java.util.List;
import java.util.Optional;

// i'm not sure if we are supposed to be providing implementations
// for testPermission(), getHelp() and getUsage().
final class SpongeCommandCallable implements CommandCallable {

    private final SpongeCommandHandler handler;

    public SpongeCommandCallable(SpongeCommandHandler handler) {
        this.handler = handler;
    }

    @Override public @NotNull CommandResult process(@NotNull CommandSource source, @NotNull String arguments) {
        CommandActor actor = new SpongeActor(source);
        handler.dispatch(actor, ArgumentStack.fromString(arguments));
        return CommandResult.empty();
    }

    @Override public @NotNull List<String> getSuggestions(@NotNull CommandSource source, @NotNull String arguments, @Nullable Location<World> targetPosition) {
        CommandActor actor = new SpongeActor(source);
        ArgumentStack args = ArgumentStack.fromString(arguments);
        return handler.getAutoCompleter().complete(actor, args);
    }

    @Override public boolean testPermission(@NotNull CommandSource source) {
        return true;
    }

    @Override public @NotNull Optional<Text> getShortDescription(@NotNull CommandSource source) {
        return Optional.empty();
    }

    @Override public @NotNull Optional<Text> getHelp(@NotNull CommandSource source) {
        return Optional.empty();
    }

    @Override public @NotNull Text getUsage(@NotNull CommandSource source) {
        return Text.of();
    }
}

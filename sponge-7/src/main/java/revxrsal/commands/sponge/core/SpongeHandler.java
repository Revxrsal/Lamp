package revxrsal.commands.sponge.core;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.world.World;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.BaseCommandHandler;
import revxrsal.commands.sponge.SpongeCommandActor;
import revxrsal.commands.sponge.SpongeCommandHandler;
import revxrsal.commands.sponge.exception.InvalidPlayerException;
import revxrsal.commands.sponge.exception.SpongeExceptionAdapter;

import static revxrsal.commands.util.Preconditions.notNull;

@ApiStatus.Internal
public class SpongeHandler extends BaseCommandHandler implements SpongeCommandHandler {

    private final Object plugin;

    public SpongeHandler(Object plugin) {
        super();
        this.plugin = notNull(plugin, "plugin");
        registerSenderResolver(SpongeSenderResolver.INSTANCE);
        registerContextValue((Class) plugin.getClass(), plugin);
        registerDependency((Class) plugin.getClass(), plugin);
        registerContextValue(Game.class, Sponge.getGame());
        registerContextValue(Server.class, Sponge.getServer());
        registerContextValue(Scheduler.class, Sponge.getScheduler());
        registerContextValue(Platform.class, Sponge.getPlatform());
        registerValueResolver(Selector.class, context -> Selector.parse(context.pop()));
        registerValueResolver(Player.class, context -> {
            String name = context.pop();
            if (name.equalsIgnoreCase("me") || name.equalsIgnoreCase("self"))
                return context.actor().as(SpongeCommandActor.class).requirePlayer();
            return Sponge.getServer().getPlayer(name)
                    .orElseThrow(() -> new InvalidPlayerException(context.parameter(), name));
        });
        registerValueResolver(World.class, context -> {
            String name = context.pop();
            if (name.equalsIgnoreCase("me") || name.equalsIgnoreCase("self"))
                return context.actor().as(SpongeCommandActor.class).requirePlayer().getWorld();
            return Sponge.getServer().getWorld(name)
                    .orElseThrow(() -> new InvalidPlayerException(context.parameter(), name));
        });
        getAutoCompleter()
                .registerSuggestion("players", SuggestionProvider.map(Sponge.getServer()::getOnlinePlayers, Player::getName))
                .registerSuggestion("worlds", SuggestionProvider.map(Sponge.getServer()::getWorlds, World::getName))
                .registerParameterSuggestions(Player.class, "players")
                .registerParameterSuggestions(World.class, "worlds");
        registerResponseHandler(String.class, (response, actor, command) -> actor.as(SpongeCommandActor.class).getSource().sendMessage(Text.of(response)));
        registerResponseHandler(Text.class, (response, actor, command) -> actor.as(SpongeCommandActor.class).getSource().sendMessage(response));
        registerResponseHandler(Text[].class, (response, actor, command) -> actor.as(SpongeCommandActor.class).getSource().sendMessages(response));
        setExceptionHandler(SpongeExceptionAdapter.INSTANCE);
    }

    @Override public @NotNull CommandHandler register(@NotNull Object... commands) {
        super.register(commands);
        for (ExecutableCommand command : executables.values()) {
            if (command.getParent() != null) continue;
            createPluginCommand(command.getName());
        }
        for (CommandCategory category : categories.values()) {
            if (category.getParent() != null) continue;
            createPluginCommand(category.getName());
        }
        return this;
    }

    private void createPluginCommand(String name) {
        CommandCallable command = new SpongeCommandCallable(this, name);
        Sponge.getCommandManager().register(plugin, command, name);
    }

    @Override public @NotNull Object getPlugin() {
        return plugin;
    }
}

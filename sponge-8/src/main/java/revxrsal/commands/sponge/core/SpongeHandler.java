package revxrsal.commands.sponge.core;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.*;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.data.Has;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.world.World;
import org.spongepowered.plugin.PluginContainer;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.BaseCommandHandler;
import revxrsal.commands.sponge.SpongeCommandActor;
import revxrsal.commands.sponge.SpongeCommandHandler;
import revxrsal.commands.sponge.exception.InvalidPlayerException;
import revxrsal.commands.sponge.exception.SpongeExceptionAdapter;

import java.util.HashMap;
import java.util.Map;

import static revxrsal.commands.util.Preconditions.notNull;

@ApiStatus.Internal
public class SpongeHandler extends BaseCommandHandler implements SpongeCommandHandler {

    private final Object plugin;

    private final Map<String, SpongeCommandCallable> registered = new HashMap<>();

    public SpongeHandler(Object plugin) {
        super();

        this.plugin = notNull(plugin, "plugin");
        registerSenderResolver(SpongeSenderResolver.INSTANCE);
        registerContextValue((Class) plugin.getClass(), plugin);
        registerDependency((Class) plugin.getClass(), plugin);
        registerContextValue(Game.class, Sponge.game());
        registerContextValue(Server.class, Sponge.server());
        registerContextValue(Scheduler.class, Sponge.asyncScheduler());
        registerContextValue(Platform.class, Sponge.platform());
        registerValueResolver(Selector.class, context -> Selector.parse(context.pop()));
        registerValueResolver(Player.class, context -> {
            String name = context.pop();
            if (name.equalsIgnoreCase("me") || name.equalsIgnoreCase("self"))
                return context.actor().as(SpongeCommandActor.class).requirePlayer();
            return Sponge.server().player(name)
                    .orElseThrow(() -> new InvalidPlayerException(context.parameter(), name));
        });
        registerValueResolver(World.class, context -> {
            String name = context.pop();
            if (name.equalsIgnoreCase("me") || name.equalsIgnoreCase("self"))
                return context.actor().as(SpongeCommandActor.class).requirePlayer().world();
            return Sponge.server().worldManager().world(ResourceKey.minecraft(name))
                    .orElseThrow(() -> new InvalidPlayerException(context.parameter(), name));
        });
        getAutoCompleter()
                .registerSuggestion("players", SuggestionProvider.map(Sponge.server()::onlinePlayers, ServerPlayer::name))
                .registerSuggestion("worlds", SuggestionProvider.map(Sponge.server().worldManager()::worlds, (world)->world.key().value()))//everything switched to ResourceKey, so we'll just leave out the namespace
                .registerParameterSuggestions(Player.class, "players")
                .registerParameterSuggestions(World.class, "worlds");
        registerResponseHandler(String.class, (response, actor, command) -> actor.as(SpongeCommandActor.class).getSource().audience().sendMessage(Component.text(response)));
        registerResponseHandler(Component.class, (response, actor, command) -> actor.as(SpongeCommandActor.class).getSource().audience().sendMessage(response));
        registerResponseHandler(Component[].class, (response, actor, command) ->{
            for(Component component : response) {
                actor.as(SpongeCommandActor.class).getSource().audience().sendMessage(component);
            }
        });
        setExceptionHandler(SpongeExceptionAdapter.INSTANCE);
        Sponge.eventManager().registerListeners((PluginContainer) plugin, this);
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
        registered.put(name, new SpongeCommandCallable(this, name));
    }

    //We have to register listeners because #Sponge8
    @Listener(order = Order.LAST)
    public void handleRegistrationEvent(final RegisterCommandEvent<Command.Raw> event) {

        this.registered.forEach((name, command)-> event.register((PluginContainer)plugin, command, name));
    }

    @Override public @NotNull Object getPlugin() {
        return plugin;
    }
}

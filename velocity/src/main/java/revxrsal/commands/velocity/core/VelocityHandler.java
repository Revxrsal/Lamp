package revxrsal.commands.velocity.core;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.BaseCommandHandler;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.velocity.PlayerSelector;
import revxrsal.commands.velocity.VelocityCommandActor;
import revxrsal.commands.velocity.VelocityCommandHandler;
import revxrsal.commands.velocity.exception.InvalidPlayerException;
import revxrsal.commands.velocity.exception.VelocityExceptionAdapter;

import static revxrsal.commands.util.Preconditions.notNull;

public final class VelocityHandler extends BaseCommandHandler implements VelocityCommandHandler {

    private final ProxyServer server;

    public VelocityHandler(@NotNull ProxyServer server) {
        super();
        this.server = notNull(server, "proxy server");
        registerPermissionReader(VelocityPermissionReader.INSTANCE);
        registerSenderResolver(VelocitySenderResolver.INSTANCE);
        registerDependency(ProxyServer.class, server);
        registerContextResolver(ProxyServer.class, ContextResolver.of(server));
        registerValueResolver(Player.class, context -> {
            String name = context.pop();
            if (name.equalsIgnoreCase("me") || name.equalsIgnoreCase("self"))
                return context.actor().as(VelocityCommandActor.class).requirePlayer();
            return server.getPlayer(name)
                    .orElseThrow(() -> new InvalidPlayerException(context.parameter(), name));
        });
        registerValueResolver(PlayerSelector.class, PlayerSelectorResolver.INSTANCE);
        getAutoCompleter()
                .registerSuggestion("players", SuggestionProvider.map(server::getAllPlayers, Player::getUsername))
                .registerParameterSuggestions(Player.class, "players")
                .registerSuggestion("playerSelector", SuggestionProvider.of("@a", "@p", "@r", "@s")
                        .compose(getAutoCompleter().getSuggestionProvider("players")))
                .registerParameterSuggestions(Player.class, "playerSelector");
        setExceptionHandler(VelocityExceptionAdapter.INSTANCE);
    }

    @Override public CommandHandler register(@NotNull Object... commands) {
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
        Command command = new VelocitySimpleCommand(this);
        server.getCommandManager().register(name, command);
    }

    @Override public ProxyServer getServer() {
        return server;
    }
}

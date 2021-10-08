package revxrsal.commands.bungee.core;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.bungee.BungeeCommandHandler;
import revxrsal.commands.bungee.PlayerSelector;
import revxrsal.commands.bungee.exception.BungeeExceptionAdapter;
import revxrsal.commands.bungee.exception.InvalidPlayerException;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.BaseCommandHandler;
import revxrsal.commands.process.ContextResolver;

import java.util.logging.Logger;

import static revxrsal.commands.util.Preconditions.notNull;

public final class BungeeHandler extends BaseCommandHandler implements BungeeCommandHandler {

    private final Plugin plugin;

    public BungeeHandler(Plugin plugin) {
        super();
        this.plugin = notNull(plugin, "plugin");
        registerSenderResolver(BungeeSenderResolver.INSTANCE);
        registerDependency((Class) plugin.getClass(), plugin);
        registerDependency(Plugin.class, plugin);

        registerValueResolver(ProxiedPlayer.class, context -> {
            String name = context.pop();
            if (name.equalsIgnoreCase("me") || name.equalsIgnoreCase("self"))
                return ((BungeeCommandActor) context.actor()).requirePlayer();
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);
            if (player == null) throw new InvalidPlayerException(context.parameter(), name);
            return player;
        });
        registerValueResolver(PlayerSelector.class, PlayerSelectorResolver.INSTANCE);
        getAutoCompleter()
                .registerSuggestion("players", SuggestionProvider.map(ProxyServer.getInstance()::getPlayers, ProxiedPlayer::getName))
                .registerParameterSuggestions(ProxiedPlayer.class, "players")
                .registerSuggestion("playerSelector", SuggestionProvider.of("@a", "@p", "@r", "@s")
                        .compose(getAutoCompleter().getSuggestionProvider("players")))
                .registerParameterSuggestions(PlayerSelector.class, "playerSelector");
        registerContextResolver(Logger.class, ContextResolver.of(plugin.getLogger()));
        registerContextResolver((Class) plugin.getClass(), ContextResolver.of(plugin));
        registerContextResolver(ProxyServer.class, ContextResolver.of(ProxyServer::getInstance));
        registerResponseHandler(BaseComponent.class, (response, subject, command) -> ((BungeeCommandActor) subject).getSender().sendMessage(response));
        registerResponseHandler(BaseComponent[].class, (response, subject, command) -> ((BungeeCommandActor) subject).getSender().sendMessage(response));
        registerResponseHandler(ComponentBuilder.class, (response, subject, command) -> ((BungeeCommandActor) subject).getSender().sendMessage(response.create()));
        registerDependency(Logger.class, plugin.getLogger());
        registerPermissionReader(BungeePermissionReader.INSTANCE);
        setExceptionHandler(BungeeExceptionAdapter.INSTANCE);
    }

    @Override public CommandHandler register(@NotNull Object... commands) {
        super.register(commands);
        for (ExecutableCommand command : registration.getExecutables().values()) {
            if (command.getParent() != null) continue;
            createPluginCommand(command.getName());
        }
        for (CommandCategory category : registration.getSubcategories().values()) {
            if (category.getParent() != null) continue;
            createPluginCommand(category.getName());
        }
        return this;
    }

    private void createPluginCommand(String name) {
        BungeeCommand command = new BungeeCommand(name, this);
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, command);
    }

    @Override public @NotNull Plugin getPlugin() {
        return plugin;
    }
}

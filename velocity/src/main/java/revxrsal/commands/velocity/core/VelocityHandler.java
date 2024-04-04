package revxrsal.commands.velocity.core;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

import java.util.Optional;

import static revxrsal.commands.util.Preconditions.notNull;

@ApiStatus.Internal
public final class VelocityHandler extends BaseCommandHandler implements VelocityCommandHandler {

    private final ProxyServer server;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<PluginContainer> plugin;
//    private final DummyVelocityBrigadier brigadier = new DummyVelocityBrigadier(this);

    public VelocityHandler(@Nullable Object plugin, @NotNull ProxyServer server) {
        super();
        this.plugin = plugin == null ? Optional.empty() : server.getPluginManager().fromInstance(plugin);
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
                .registerParameterSuggestions(PlayerSelector.class, "playerSelector");
        registerResponseHandler(ComponentLike.class, (component, actor, command) -> actor.as(VelocityCommandActor.class).reply(component));
        setExceptionHandler(VelocityExceptionAdapter.INSTANCE);
    }

    @Override public @NotNull CommandHandler register(@NotNull Object... commands) {
        super.register(commands);
        for (ExecutableCommand command : executables.values()) {
            if (command.getParent() != null) continue;
            createPluginCommand(command);
        }
        for (CommandCategory category : categories.values()) {
            if (category.getParent() != null) continue;
            createPluginCommand(category);
        }
        return this;
    }

    private void createPluginCommand(Object commandComponent) {
        Command command = new VelocitySimpleCommand(this);
        if (commandComponent instanceof CommandCategory) {
            CommandCategory category = ((CommandCategory) commandComponent);
//            registerNode(command, parse(brigadier, literal(category.getName()), category));
            registerNode(category.getName(), command);
            if (getNamespace() != null)
//                registerNode(command, parse(brigadier, literal(getNamespace() + ":" + category.getName()), category));
                registerNode(getNamespace() + ":" + category.getName(), command);
        } else if (commandComponent instanceof ExecutableCommand) {
            ExecutableCommand executable = ((ExecutableCommand) commandComponent);
//            registerNode(command, parse(brigadier, literal(executable.getName()), executable));
            registerNode(executable.getName(), command);
            if (getNamespace() != null)
//                registerNode(command, parse(brigadier, literal(getNamespace() + ":" + executable.getName()), executable));
                registerNode(getNamespace() + ":" + executable.getName(), command);
        }
    }

//    private void registerNode(Command command, LiteralArgumentBuilder<CommandSource> builder) {
//        CommandMeta.Builder metaBuilder = metaBuilder(builder.getLiteral());
//        LiteralCommandNode<CommandSource> node = builder.build();
//        node.getChildren().forEach(metaBuilder::hint);
//        getArguments(node).values().forEach(metaBuilder::hint);
//        server.getCommandManager().register(metaBuilder.build(), command);
//    }

    private void registerNode(String alias, Command command) {
        server.getCommandManager().register(alias, command);
    }

    private @Nullable String getNamespace() {
        return plugin.map(p -> p.getDescription().getId()).orElse(null);
    }

    private CommandMeta.Builder metaBuilder(String alias) {
        return server.getCommandManager().metaBuilder(alias);
    }

    @Override public ProxyServer getServer() {
        return server;
    }

}

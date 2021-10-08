package revxrsal.commands.bukkit.core;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.PlayerSelector;
import revxrsal.commands.bukkit.exception.BukkitExceptionAdapter;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.bukkit.exception.InvalidWorldException;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.BaseCommandHandler;
import revxrsal.commands.core.CommandPath;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static revxrsal.commands.util.Preconditions.notNull;

public final class BukkitHandler extends BaseCommandHandler implements BukkitCommandHandler {

    private final Plugin plugin;

    public BukkitHandler(@NotNull Plugin plugin) {
        super();
        this.plugin = notNull(plugin, "plugin");
        registerSenderResolver(BukkitSenderResolver.INSTANCE);
        registerValueResolver(Player.class, context -> {
            String value = context.pop();
            if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me"))
                return ((BukkitCommandActor) context.actor()).requirePlayer();
            Player player = Bukkit.getPlayer(value);
            if (player == null)
                throw new InvalidPlayerException(context.parameter(), value);
            return player;
        });
        registerValueResolver(OfflinePlayer.class, context -> {
            String value = context.pop();
            if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me"))
                return ((BukkitCommandActor) context.actor()).requirePlayer();
            //noinspection deprecation
            OfflinePlayer player = Bukkit.getOfflinePlayer(value);
            if (!player.hasPlayedBefore())
                throw new InvalidPlayerException(context.parameter(), value);
            return player;
        });
        registerValueResolver(World.class, context -> {
            String value = context.pop();
            if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me"))
                return ((BukkitCommandActor) context.actor()).requirePlayer().getWorld();
            World world = Bukkit.getWorld(value);
            if (world == null)
                throw new InvalidWorldException(context.parameter(), value);
            return world;
        });
        registerValueResolver(PlayerSelector.class, PlayerSelectorResolver.INSTANCE);
        getAutoCompleter().registerSuggestion("players", (args, sender, command) -> Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> !((BukkitCommandActor) sender).isPlayer() || ((BukkitCommandActor) sender).requirePlayer().canSee(player))
                .map(HumanEntity::getName)
                .collect(Collectors.toList()));
        getAutoCompleter()
                .registerSuggestion("worlds", SuggestionProvider.map(Bukkit::getWorlds, World::getName))
                .registerParameterSuggestions(Player.class, "players")
                .registerParameterSuggestions(World.class, "worlds")
                .registerSuggestion("playerSelector",
                        SuggestionProvider.of("@a", "@p", "@r", "@s")
                                .compose(getAutoCompleter().getSuggestionProvider("players")))
                .registerParameterSuggestions(PlayerSelector.class, "playerSelector");

        registerContextValue((Class) plugin.getClass(), plugin);
        registerDependency((Class) plugin.getClass(), plugin);
        registerDependency(FileConfiguration.class, plugin.getConfig());
        registerDependency(Logger.class, plugin.getLogger());
        registerPermissionReader(BukkitPermissionReader.INSTANCE);
        setExceptionHandler(BukkitExceptionAdapter.INSTANCE);
    }

    @Override public CommandHandler register(@NotNull Object... commands) {
        super.register(commands);
        for (ExecutableCommand command : registration.getExecutables().values()) {
            if (command.getParent() != null) continue;
            createPluginCommand(command.getName(), command.getDescription(), command.getUsage());
        }
        for (CommandCategory category : registration.getSubcategories().values()) {
            if (category.getParent() != null) continue;
            createPluginCommand(category.getName(), null, null);
        }
        return this;
    }

    @Override public @NotNull Plugin getPlugin() {
        return plugin;
    }

    private @SneakyThrows void createPluginCommand(String name, @Nullable String description, @Nullable String usage) {
        PluginCommand cmd = COMMAND_CONSTRUCTOR.newInstance(name, plugin);
        COMMAND_MAP.register(plugin.getName(), cmd);
        BukkitCommandExecutor executor = new BukkitCommandExecutor(this);
        cmd.setExecutor(executor);
        cmd.setTabCompleter(executor);
        cmd.setDescription(description == null ? "" : description);
        if (usage != null)
            cmd.setUsage(usage);
    }

    @Override public boolean unregister(@NotNull CommandPath path) {
        for (CommandCategory c : registration.getSubcategories().values()) {
            if (c.getParent() == null) {
                PluginCommand command = ((JavaPlugin) plugin).getCommand(c.getName());
                if (command != null) command.unregister(COMMAND_MAP);
            }
        }
        return super.unregister(path);
    }

    private static final Constructor<PluginCommand> COMMAND_CONSTRUCTOR;
    private static final CommandMap COMMAND_MAP;

    static {
        Constructor<PluginCommand> ctr;
        CommandMap commandMap;
        try {
            ctr = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            ctr.setAccessible(true);
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Unable to access PluginCommand(String, Plugin) construtor!");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to access Bukkit.getServer()#commandMap!");
        }
        COMMAND_CONSTRUCTOR = ctr;
        COMMAND_MAP = commandMap;
    }
}

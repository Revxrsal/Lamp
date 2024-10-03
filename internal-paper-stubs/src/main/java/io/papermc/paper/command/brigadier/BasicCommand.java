package io.papermc.paper.command.brigadier;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementing this interface allows for easily creating "Bukkit-style" {@code String[] args} commands.
 * The implementation handles converting the command to a representation compatible with Brigadier on registration, usually in the form of {@literal /commandlabel <greedy_string>}.
 */
@ApiStatus.Experimental
@FunctionalInterface
public interface BasicCommand {

    /**
     * Executes the command with the given {@link CommandSourceStack} and arguments.
     *
     * @param commandSourceStack the commandSourceStack of the command
     * @param args               the arguments of the command ignoring repeated spaces
     */
    @ApiStatus.OverrideOnly
    void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args);

    /**
     * Suggests possible completions for the given command {@link CommandSourceStack} and arguments.
     *
     * @param commandSourceStack the commandSourceStack of the command
     * @param args               the arguments of the command including repeated spaces
     * @return a collection of suggestions
     */
    @ApiStatus.OverrideOnly
    default @NotNull Collection<String> suggest(final @NotNull CommandSourceStack commandSourceStack, final @NotNull String[] args) {
        return Collections.emptyList();
    }

    /**
     * Checks whether a command sender can receive and run the root command.
     *
     * @param sender the command sender trying to execute the command
     * @return whether the command sender fulfills the root command requirement
     * @see #permission()
     */
    @ApiStatus.OverrideOnly
    default boolean canUse(final @NotNull CommandSender sender) {
        return this.permission() == null || sender.hasPermission(this.permission());
    }

    /**
     * Returns the permission for the root command used in {@link #canUse(CommandSender)} by default.
     *
     * @return the permission for the root command used in {@link #canUse(CommandSender)}
     */
    @ApiStatus.OverrideOnly
    default @Nullable String permission() {
        return null;
    }
}

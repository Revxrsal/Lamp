package revxrsal.commands.jda.core.adapter;

import static revxrsal.commands.util.Preconditions.notNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataObject;

/**
 * Adapts {@link SlashCommandData} and {@link SubcommandData} into one object.
 */
public interface SlashCommandAdapter {
    /**
     * Creates a new {@link SlashCommandAdapter} that wraps the given {@link SlashCommandData}
     *
     * @param slashCommandData Slash command to wrap
     * @return The wrapping {@link SlashCommandAdapter} instance
     */
    @NotNull static SlashCommandAdapter of(@NotNull SlashCommandData slashCommandData) {
        return new BaseSlashCommandAdapter(slashCommandData);
    }

    /**
     * Creates a new {@link SlashCommandAdapter} that wraps the given {@link SubcommandData}
     *
     * @param subcommandData Slash command to wrap
     * @return The wrapping {@link SlashCommandAdapter} instance
     */
    @NotNull static SlashCommandAdapter of(@NotNull SubcommandData subcommandData) {
        return new BaseSubcommandToSlashAdapter(subcommandData);
    }

    /**
     * Returns original {@link SlashCommandData} or {@link SubcommandData}.
     *
     * @return Wrapping object instance. May be {@link SlashCommandData}, {@link SubcommandData}
     * @see #isSlashCommand()
     * @see #isSlashSubcommand()
     */
    <T> T getCommandData();

    /**
     * Returns {@code true} if {@link #getCommandData()} instance of {@link SlashCommandData}
     *
     * @return true if wrapping object is {@link SlashCommandData}
     * @see #getCommandData()
     */
    default boolean isSlashCommand() {
        Object commandData = getCommandData();
        return commandData instanceof SlashCommandData;
    }

    /**
     * Returns {@code true} if {@link #getCommandData()} instance of {@link SubcommandData}
     *
     * @return true if wrapping object is {@link SubcommandData}
     * @see #getCommandData()
     */
    default boolean isSlashSubcommand() {
        Object commandData = getCommandData();
        return commandData instanceof SubcommandData;
    }

    /**
     * Sets a {@link DiscordLocale language-specific} localization of this {@link #getCommandData()} name.
     *
     * @param  locale
     *         The locale to associate the translated name with
     *
     * @param  name
     *         The translated name to put
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the name is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *             <li>If the name does not pass the corresponding {@link #setName(String) name check}</li>
     *         </ul>
     *
     * @return This adapter instance, for chaining
     */
    @NotNull SlashCommandAdapter setNameLocalization(@NotNull DiscordLocale locale, @NotNull String name);

    /**
     * Sets a {@link DiscordLocale language-specific} localizations of this {@link #getCommandData()} description.
     *
     * @param  locale
     *         The locale to associate the translated description with
     *
     * @param  description
     *         The translated description to put
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the description is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *             <li>If the description does not pass the corresponding {@link #setDescription(String) description check}</li>
     *         </ul>
     *
     * @return This adapter instance, for chaining
     */
    @NotNull SlashCommandAdapter setDescriptionLocalization(@NotNull DiscordLocale locale, @NotNull String description);

    /**
     * Removes all options that evaluate to {@code true} under the provided {@code condition}.
     * <br>This will not affect options within subcommands.
     * Use {@link SubcommandData#removeOptions(Predicate)} instead.
     *
     * <p><b>Example: Remove all options</b>
     * <pre>{@code
     * command.removeOptions(option -> true);
     * }</pre>
     * <p><b>Example: Remove all options that are required</b>
     * <pre>{@code
     * command.removeOptions(option -> option.isRequired());
     * }</pre>
     *
     * @param  condition
     *         The removal condition (must not throw)
     *
     * @throws IllegalArgumentException
     *         If the condition is null
     *
     * @return True, if any options were removed
     */
    boolean removeOptions(@NotNull Predicate<OptionData> condition);

    /**
     * Removes options by the provided name.
     * <br>This will not affect options within subcommands.
     * Use {@link SubcommandData#removeOptionByName(String)} instead.
     *
     * @param  name
     *         The <b>case-sensitive</b> option name
     *
     * @return True, if any options were removed
     */
    default boolean removeOptionByName(@NotNull String name) {
        return removeOptions(option -> option.getName().equals(name));
    }

    /**
     * Adds up to {@value CommandData#MAX_OPTIONS} options to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *          The {@link OptionData Options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return This adapter instance, for chaining
     */
    @NotNull SlashCommandAdapter addOptions(OptionData... options);

    /**
     * Adds up to {@value CommandData#MAX_OPTIONS} options to this {@link #getCommandData()}.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The {@link OptionData Options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return This adapter instance, for chaining
     */
    default @NotNull SlashCommandAdapter addOptions(@NotNull Collection<OptionData> options) {
        notNull(options, "options");
        return addOptions(options.toArray(new OptionData[0]));
    }

    /**
     * Adds an option to this {@link #getCommandData()}.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-{@value OptionData#MAX_NAME_LENGTH} characters
     * @param  description
     *         The option description, 1-{@value OptionData#MAX_DESCRIPTION_LENGTH} characters
     * @param  required
     *         Whether this option is required (See {@link OptionData#setRequired(boolean)})
     * @param  autoComplete
     *         Whether this option supports auto-complete via {@link CommandAutoCompleteInteractionEvent},
     *         only supported for option types which {@link OptionType#canSupportChoices() support choices}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command.</li>
     *             <li>If the option type is {@link OptionType#UNKNOWN UNKNOWN}.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If the provided option type does not support auto-complete</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return This adapter instance, for chaining
     */
    @NotNull SlashCommandAdapter addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description, boolean required, boolean autoComplete);

    /**
     * Adds an option to this {@link #getCommandData()}.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-{@value OptionData#MAX_NAME_LENGTH} characters
     * @param  description
     *         The option description, 1-{@value OptionData#MAX_DESCRIPTION_LENGTH} characters
     * @param  required
     *         Whether this option is required (See {@link OptionData#setRequired(boolean)})
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return This adapter instance, for chaining
     */
    default @NotNull SlashCommandAdapter addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description, boolean required) {
        return addOption(type, name, description, required, false);
    }

    /**
     * Adds an option to this {@link #getCommandData()}.
     * <br>The option is set to be non-required! You can use {@link #addOption(OptionType, String, String, boolean)} to add a required option instead.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-{@value OptionData#MAX_NAME_LENGTH} characters
     * @param  description
     *         The option description, 1-{@value OptionData#MAX_DESCRIPTION_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return This adapter instance, for chaining
     */
    default @NotNull SlashCommandAdapter addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description) {
        return addOption(type, name, description, false);
    }

    /**
     * The options for this command.
     *
     * @return Immutable list of {@link OptionData}
     */
    @NotNull @UnmodifiableView List<OptionData> getOptions();

    /**
     * The configured name
     *
     * @return The name
     */
    @NotNull String getName();

    /**
     * Configure the name
     *
     * @param  name
     *         The lowercase alphanumeric (with dash) name, 1-32 characters
     *
     * @throws IllegalArgumentException
     *         If the name is null, not alphanumeric, or not between 1-32 characters
     *
     * @return This adapter instance, for chaining
     */
    @NotNull SlashCommandAdapter setName(@NotNull String name);

    /**
     * The localizations of this subcommand's name for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized name
     */
    @NotNull LocalizationMap getNameLocalizations();

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this {@link #getCommandData()} name.
     *
     * @param  map
     *         The map from which to transfer the translated names
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link DiscordLocale#UNKNOWN} key</li>
     *             <li>If the map contains a name which does not pass the corresponding {@link #setName(String) name check}</li>
     *         </ul>
     *
     * @return This adapter instance, for chaining
     */
    @NotNull SlashCommandAdapter setNameLocalizations(@NotNull Map<DiscordLocale, String> map);

    /**
     * The configured description
     *
     * @return The description
     */
    @NotNull String getDescription();

    /**
     * Configure the description
     *
     * @param  description
     *         The description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         If the name is null or not between 1-100 characters
     *
     * @return This adapter instance, for chaining
     */
    @NotNull SlashCommandAdapter setDescription(@NotNull String description);

    /**
     * The localizations of this {@link #getCommandData()} description for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized description
     */
    @NotNull LocalizationMap getDescriptionLocalizations();

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this {@link #getCommandData()} description.
     *
     * @param  map
     *         The map from which to transfer the translated descriptions
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link DiscordLocale#UNKNOWN} key</li>
     *             <li>If the map contains a description which does not pass the corresponding {@link #setDescription(String) description check}</li>
     *         </ul>
     *
     * @return This adapter instance, for chaining
     */
    @NotNull SlashCommandAdapter setDescriptionLocalizations(@NotNull Map<DiscordLocale, String> map);

    @NotNull DataObject toData();
}

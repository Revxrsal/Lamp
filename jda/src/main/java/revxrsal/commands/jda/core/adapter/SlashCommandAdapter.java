package revxrsal.commands.jda.core.adapter;

import static revxrsal.commands.util.Preconditions.notNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataObject;

public interface SlashCommandAdapter {
    @NotNull static SlashCommandAdapter of(@NotNull SlashCommandData slashCommandData) {
        return new BaseSlashCommandAdapter(slashCommandData);
    }

    @NotNull static SlashCommandAdapter of(@NotNull SubcommandData subcommandData) {
        return new BaseSubcommandToSlashAdapter(subcommandData);
    }

    <T> T getCommandData();

    default boolean isSlashCommand() {
        Object commandData = getCommandData();
        return commandData instanceof SlashCommandData;
    }

    default boolean isSlashSubcommand() {
        Object commandData = getCommandData();
        return commandData instanceof SubcommandData;
    }

    @NotNull SlashCommandAdapter setNameLocalization(@NotNull DiscordLocale locale, @NotNull String name);

    @NotNull SlashCommandAdapter setDescriptionLocalization(@NotNull DiscordLocale locale, @NotNull String description);

    boolean removeOptions(@NotNull Predicate<OptionData> condition);

    default boolean removeOptionByName(@NotNull String name) {
        return removeOptions(option -> option.getName().equals(name));
    }

    @NotNull SlashCommandAdapter addOptions(OptionData... options);

    default @NotNull SlashCommandAdapter addOptions(@NotNull Collection<OptionData> options) {
        notNull(options, "options");
        return addOptions(options.toArray(new OptionData[0]));
    }

    @NotNull SlashCommandAdapter addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description, boolean required, boolean autoComplete);

    default SlashCommandAdapter addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description, boolean required) {
        return addOption(type, name, description, required, false);
    }

    default SlashCommandAdapter addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description) {
        return addOption(type, name, description, false);
    }

    @NotNull @UnmodifiableView List<OptionData> getOptions();

    @NotNull String getName();

    @NotNull SlashCommandAdapter setName(@NotNull String name);

    @NotNull LocalizationMap getNameLocalizations();

    @NotNull SlashCommandAdapter setNameLocalizations(@NotNull Map<DiscordLocale, String> map);

    @NotNull String getDescription();

    @NotNull SlashCommandAdapter setDescription(@NotNull String description);

    @NotNull LocalizationMap getDescriptionLocalizations();

    @NotNull SlashCommandAdapter setDescriptionLocalizations(@NotNull Map<DiscordLocale, String> map);

    @NotNull DataObject toData();
}

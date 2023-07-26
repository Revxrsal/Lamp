package revxrsal.commands.jda.core.adapter;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataObject;

public class BaseSubcommandToSlashAdapter implements SlashCommandAdapter {
    private final SubcommandData subcommandData;

    public BaseSubcommandToSlashAdapter(@NotNull SubcommandData subcommandData) {
        this.subcommandData = subcommandData;
    }

    @Override
    public <T> T getCommandData() {
        return (T) subcommandData;
    }

    @Override
    public @NotNull SlashCommandAdapter setNameLocalization(@NotNull DiscordLocale locale, @NotNull String name) {
        subcommandData.setNameLocalization(locale, name);
        return this;
    }

    @Override
    public @NotNull SlashCommandAdapter setDescriptionLocalization(@NotNull DiscordLocale locale, @NotNull String description) {
        subcommandData.setDescriptionLocalization(locale, description);
        return this;
    }

    @Override
    public boolean removeOptions(@NotNull Predicate<OptionData> condition) {
        return subcommandData.removeOptions(condition);
    }

    @Override
    public @NotNull SlashCommandAdapter addOptions(OptionData... options) {
        subcommandData.addOptions(options);
        return this;
    }

    @Override
    public @NotNull SlashCommandAdapter addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description, boolean required,
                                                  boolean autoComplete) {
        subcommandData.addOption(type, name, description, required, autoComplete);
        return this;
    }

    @Override
    public @NotNull @UnmodifiableView List<OptionData> getOptions() {
        return subcommandData.getOptions();
    }

    @Override
    public @NotNull String getName() {
        return subcommandData.getName();
    }

    @Override
    public @NotNull SlashCommandAdapter setName(@NotNull String name) {
        subcommandData.setName(name);
        return this;
    }

    @Override
    public @NotNull LocalizationMap getNameLocalizations() {
        return subcommandData.getNameLocalizations();
    }

    @Override
    public @NotNull SlashCommandAdapter setNameLocalizations(@NotNull Map<DiscordLocale, String> map) {
        subcommandData.setNameLocalizations(map);
        return this;
    }

    @Override
    public @NotNull String getDescription() {
        return subcommandData.getDescription();
    }

    @Override
    public @NotNull SlashCommandAdapter setDescription(@NotNull String description) {
        subcommandData.setDescription(description);
        return this;
    }

    @Override
    public @NotNull LocalizationMap getDescriptionLocalizations() {
        return subcommandData.getDescriptionLocalizations();
    }

    @Override
    public @NotNull SlashCommandAdapter setDescriptionLocalizations(@NotNull Map<DiscordLocale, String> map) {
        subcommandData.setDescriptionLocalizations(map);
        return this;
    }

    @Override
    public @NotNull DataObject toData() {
        return subcommandData.toData();
    }
}

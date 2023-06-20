package revxrsal.commands.jda.core.adapter;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataObject;

public class BaseSlashCommandAdapter implements SlashCommandAdapter {
    private final SlashCommandData slashCommandData;

    public BaseSlashCommandAdapter(@NotNull SlashCommandData slashCommandData) {
        this.slashCommandData = slashCommandData;
    }

    @Override
    public <T> T getCommandData() {
        return (T) slashCommandData;
    }

    @Override
    public @NotNull SlashCommandAdapter setNameLocalization(@NotNull DiscordLocale locale, @NotNull String name) {
        slashCommandData.setNameLocalization(locale, name);
        return this;
    }

    @Override
    public @NotNull SlashCommandAdapter setDescriptionLocalization(@NotNull DiscordLocale locale, @NotNull String description) {
        slashCommandData.setDescriptionLocalization(locale, description);
        return this;
    }

    @Override
    public boolean removeOptions(@NotNull Predicate<OptionData> condition) {
        return slashCommandData.removeOptions(condition);
    }

    @Override
    public @NotNull SlashCommandAdapter addOptions(OptionData... options) {
        slashCommandData.addOptions(options);
        return this;
    }

    @Override
    public @NotNull SlashCommandAdapter addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description, boolean required,
                                                  boolean autoComplete) {
        slashCommandData.addOption(type, name, description, required, autoComplete);
        return this;
    }

    @Override
    public @NotNull @UnmodifiableView List<OptionData> getOptions() {
        return slashCommandData.getOptions();
    }

    @Override
    public @NotNull String getName() {
        return slashCommandData.getName();
    }

    @Override
    public @NotNull SlashCommandAdapter setName(@NotNull String name) {
        slashCommandData.setName(name);
        return this;
    }

    @Override
    public @NotNull LocalizationMap getNameLocalizations() {
        return slashCommandData.getNameLocalizations();
    }

    @Override
    public @NotNull SlashCommandAdapter setNameLocalizations(@NotNull Map<DiscordLocale, String> map) {
        slashCommandData.setNameLocalizations(map);
        return this;
    }

    @Override
    public @NotNull String getDescription() {
        return slashCommandData.getDescription();
    }

    @Override
    public @NotNull SlashCommandAdapter setDescription(@NotNull String description) {
        slashCommandData.setDescription(description);
        return this;
    }

    @Override
    public @NotNull LocalizationMap getDescriptionLocalizations() {
        return slashCommandData.getDescriptionLocalizations();
    }

    @Override
    public @NotNull SlashCommandAdapter setDescriptionLocalizations(@NotNull Map<DiscordLocale, String> map) {
        slashCommandData.setDescriptionLocalizations(map);
        return this;
    }

    @Override
    public @NotNull DataObject toData() {
        return slashCommandData.toData();
    }
}

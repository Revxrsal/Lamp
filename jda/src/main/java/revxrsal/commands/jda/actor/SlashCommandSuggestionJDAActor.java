package revxrsal.commands.jda.actor;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import revxrsal.commands.jda.JDAActor;

public interface SlashCommandSuggestionJDAActor extends JDAActor {
    default @UnmodifiableView @NotNull List<OptionMapping> getOptions(){
        return getSuggestionEvent().getOptions();
    }

    default @NotNull AutoCompleteQuery getFocusedOption(){
        return getSuggestionEvent().getFocusedOption();
    }

    /**
     * Returns the cast result of {@link #getGenericEvent()} to {@link CommandAutoCompleteInteractionEvent}.
     *
     * @return The event
     */
    default @NotNull CommandAutoCompleteInteractionEvent getSuggestionEvent() {
        return (CommandAutoCompleteInteractionEvent) getGenericEvent();
    }
}

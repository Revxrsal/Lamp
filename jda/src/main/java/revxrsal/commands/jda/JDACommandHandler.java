package revxrsal.commands.jda;

import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.jda.core.JDAHandler;

/**
 * Represents JDA's command handler implementation
 */
public interface JDACommandHandler extends CommandHandler {

    @NotNull JDA getJDA();

    static @NotNull JDACommandHandler create(@NotNull JDA jda) {
        return create(jda, "/");
    }

    static @NotNull JDACommandHandler create(@NotNull JDA jda, String prefix) {
        return new JDAHandler(jda, prefix);
    }

}

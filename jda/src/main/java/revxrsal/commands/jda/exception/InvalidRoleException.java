package revxrsal.commands.jda.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;
import revxrsal.commands.jda.JDAActor;

public class InvalidRoleException extends InvalidValueException {

    public InvalidRoleException(@NotNull CommandParameter parameter, @NotNull String input, @NotNull CommandActor actor) {
        super(parameter, input, actor);
    }

    @Override public @NotNull JDAActor getActor() {
        return super.getActor().as(JDAActor.class);
    }
}

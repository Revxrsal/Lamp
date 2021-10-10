package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Default implementation of {@link CommandExceptionHandler}, which sends basic messages
 * describing the exception.
 */
public class DefaultExceptionHandler extends CommandExceptionAdapter {

    public static final DefaultExceptionHandler INSTANCE = new DefaultExceptionHandler();

    @Override protected void missingArgument(@NotNull CommandActor actor, @NotNull MissingArgumentException exception) {
        actor.error("You must specify a value for the " + exception.getParameter().getName() + "!");
    }

    @Override protected void invalidEnumValue(@NotNull CommandActor actor, @NotNull EnumNotFoundException exception) {
        actor.error("Invalid " + exception.getParameter().getName() + ": " + exception.getInput() + ".");
    }

    @Override protected void invalidNumber(@NotNull CommandActor actor, @NotNull InvalidNumberException exception) {
        actor.error("Expected a number, but found '" + exception.getInput() + "'.");
    }

    @Override protected void invalidUUID(@NotNull CommandActor actor, @NotNull InvalidUUIDException exception) {
        actor.error("Invalid UUID: " + exception.getInput());
    }

    @Override protected void invalidURL(@NotNull CommandActor actor, @NotNull InvalidURLException exception) {
        actor.error("Invalid URL: " + exception.getInput());
    }

    @Override protected void invalidBoolean(@NotNull CommandActor actor, @NotNull InvalidBooleanException exception) {
        actor.error("Expected true or false, but found '" + exception.getInput() + "'.");
    }

    @Override protected void noPermission(@NotNull CommandActor actor, @NotNull NoPermissionException exception) {
        actor.error("You do not have permission to execute this command!");
    }

    @Override protected void commandInvocation(@NotNull CommandActor actor, @NotNull CommandInvocationException exception) {
        actor.error("An error occurred while executing the command.");
        exception.getCause().printStackTrace();
    }

    @Override protected void tooManyArguments(@NotNull CommandActor actor, @NotNull TooManyArgumentsException exception) {
        ExecutableCommand command = exception.getCommand();
        actor.error("Too many arguments! Correct usage: /" + command.getPath().toRealString() + " " + command.getUsage());
    }

    @Override protected void invalidCommand(@NotNull CommandActor actor, @NotNull InvalidCommandException exception) {
        actor.error("Invalid command: " + exception.getInput());
    }

    @Override protected void invalidSubcommand(@NotNull CommandActor actor, @NotNull InvalidSubcommandException exception) {
        actor.error("Invalid subcommand: " + exception.getInput());
    }

    @Override protected void noSubcommandSpecified(@NotNull CommandActor actor, @NotNull NoSubcommandSpecifiedException exception) {
        actor.error("You must specify a subcommand!");
    }

    @Override protected void cooldown(@NotNull CommandActor actor, @NotNull CooldownException exception) {
        actor.error("You must wait " + formatTimeFancy(exception.getTimeLeftMillis()) + " before using this command again.");
    }

    public static String formatTimeFancy(long time) {
        Duration d = Duration.ofMillis(time);
        long hours = d.toHours();
        long minutes = d.minusHours(hours).getSeconds() / 60;
        long seconds = d.minusMinutes(minutes).minusHours(hours).getSeconds();
        List<String> words = new ArrayList<>();
        if (hours != 0)
            words.add(hours + plural(hours, " hour"));
        if (minutes != 0)
            words.add(minutes + plural(minutes, " minute"));
        if (seconds != 0)
            words.add(seconds + plural(seconds, " second"));
        return toFancyString(words);
    }

    public static <T> String toFancyString(List<T> list) {
        StringJoiner builder = new StringJoiner(", ");
        if (list.isEmpty()) return "";
        if (list.size() == 1) return list.get(0).toString();
        for (int i = 0; i < list.size(); i++) {
            T el = list.get(i);
            if (i + 1 == list.size())
                return builder + " and " + el.toString();
            else
                builder.add(el.toString());
        }
        return builder.toString();
    }

    public static String plural(Number count, String thing) {
        if (count.intValue() == 1) return thing;
        if (thing.endsWith("y"))
            return thing.substring(0, thing.length() - 1) + "ies";
        return thing + "s";
    }

    @Override protected void handleUnknown(@NotNull CommandActor actor, @NotNull Throwable throwable) {
        throwable.printStackTrace();
    }
}

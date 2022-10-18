package revxrsal.commands.exception;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;

/**
 * Default implementation of {@link CommandExceptionHandler}, which sends basic messages describing
 * the exception.
 * <p>
 * See {@link CommandExceptionAdapter} for handling custom exceptions.
 */
public class DefaultExceptionHandler extends CommandExceptionAdapter {

  public static final DefaultExceptionHandler INSTANCE = new DefaultExceptionHandler();
  public static final NumberFormat FORMAT = NumberFormat.getInstance();

  @Override
  public void missingArgument(@NotNull final CommandActor actor,
      @NotNull final MissingArgumentException exception) {
    actor.errorLocalized("commands.missing-argument", exception.getParameter().getName());
  }

  @Override
  public void invalidEnumValue(@NotNull final CommandActor actor,
      @NotNull final EnumNotFoundException exception) {
    actor.errorLocalized("commands.invalid-enum", exception.getParameter().getName(),
        exception.getInput());
  }

  @Override
  public void invalidNumber(@NotNull final CommandActor actor,
      @NotNull final InvalidNumberException exception) {
//        actor.error("Expected a number, but found '" + exception.getInput() + "'.");
    actor.errorLocalized("commands.invalid-number", exception.getInput());
  }

  @Override
  public void invalidUUID(@NotNull final CommandActor actor,
      @NotNull final InvalidUUIDException exception) {
    actor.errorLocalized("commands.invalid-uuid", exception.getInput());
  }

  @Override
  public void invalidURL(@NotNull final CommandActor actor,
      @NotNull final InvalidURLException exception) {
    actor.errorLocalized("commands.invalid-url", exception.getInput());
  }

  @Override
  public void invalidBoolean(@NotNull final CommandActor actor,
      @NotNull final InvalidBooleanException exception) {
    actor.errorLocalized("commands.invalid-boolean", exception.getInput());
  }

  @Override
  public void noPermission(@NotNull final CommandActor actor,
      @NotNull final NoPermissionException exception) {
    actor.errorLocalized("commands.no-permission");
  }

  @Override
  public void argumentParse(@NotNull final CommandActor actor,
      @NotNull final ArgumentParseException exception) {
    actor.errorLocalized("commands.invalid-quoted-string");
    actor.error(exception.getSourceString());
    actor.error(exception.getAnnotatedPosition());
  }

  @Override
  public void commandInvocation(@NotNull final CommandActor actor,
      @NotNull final CommandInvocationException exception) {
    actor.errorLocalized("commands.error-occurred");
    exception.getCause().printStackTrace();
  }

  @Override
  public void tooManyArguments(@NotNull final CommandActor actor,
      @NotNull final TooManyArgumentsException exception) {
    final ExecutableCommand command = exception.getCommand();
    final String usage = (command.getPath().toRealString() + " " + command.getUsage()).trim();
    actor.errorLocalized("commands.too-many-arguments", usage);
  }

  @Override
  public void invalidCommand(@NotNull final CommandActor actor,
      @NotNull final InvalidCommandException exception) {
    actor.errorLocalized("commands.invalid-command", exception.getInput());
  }

  @Override
  public void invalidSubcommand(@NotNull final CommandActor actor,
      @NotNull final InvalidSubcommandException exception) {
    actor.errorLocalized("commands.invalid-subcommand", exception.getInput());
  }

  @Override
  public void noSubcommandSpecified(@NotNull final CommandActor actor,
      @NotNull final NoSubcommandSpecifiedException exception) {
    actor.errorLocalized("commands.no-subcommand-specified");
  }

  @Override
  public void cooldown(@NotNull final CommandActor actor,
      @NotNull final CooldownException exception) {
    actor.errorLocalized("commands.on-cooldown", formatTimeFancy(exception.getTimeLeftMillis()));
  }

  @Override
  public void invalidHelpPage(@NotNull final CommandActor actor,
      @NotNull final InvalidHelpPageException exception) {
    actor.errorLocalized("commands.invalid-help-page", exception.getPage(),
        exception.getPageCount());
  }

  @Override
  public void sendableException(@NotNull final CommandActor actor,
      @NotNull final SendableException exception) {
    exception.sendTo(actor);
  }

  @Override
  public void numberNotInRange(@NotNull final CommandActor actor,
      @NotNull final NumberNotInRangeException exception) {
    actor.errorLocalized("commands.number-not-in-range",
        exception.getParameter().getName(),
        FORMAT.format(exception.getMinimum()),
        FORMAT.format(exception.getMaximum()),
        FORMAT.format(exception.getInput())
    );
  }

  @Override
  public void onUnhandledException(@NotNull final CommandActor actor,
      @NotNull final Throwable throwable) {
    throwable.printStackTrace();
  }

  public static String formatTimeFancy(final long time) {
    final Duration d = Duration.ofMillis(time);
    final long hours = d.toHours();
    final long minutes = d.minusHours(hours).getSeconds() / 60;
    final long seconds = d.minusMinutes(minutes).minusHours(hours).getSeconds();
    final List<String> words = new ArrayList<>();
    if (hours != 0) {
      words.add(hours + plural(hours, " hour"));
    }
    if (minutes != 0) {
      words.add(minutes + plural(minutes, " minute"));
    }
    if (seconds != 0) {
      words.add(seconds + plural(seconds, " second"));
    }
    return toFancyString(words);
  }

  public static <T> String toFancyString(final List<T> list) {
    final StringJoiner builder = new StringJoiner(", ");
    if (list.isEmpty()) {
      return "";
    }
    if (list.size() == 1) {
      return list.get(0).toString();
    }
    for (int i = 0; i < list.size(); i++) {
      final T el = list.get(i);
      if (i + 1 == list.size()) {
        return builder + " and " + el.toString();
      } else {
        builder.add(el.toString());
      }
    }
    return builder.toString();
  }

  public static String plural(final Number count, final String thing) {
    if (count.intValue() == 1) {
      return thing;
    }
    if (thing.endsWith("y")) {
      return thing.substring(0, thing.length() - 1) + "ies";
    }
    return thing + "s";
  }
}

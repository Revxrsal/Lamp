package dev.demeng.pluginbase.commands.bukkit.exception;

import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.exception.DefaultExceptionHandler;
import org.jetbrains.annotations.NotNull;

public class BukkitExceptionAdapter extends DefaultExceptionHandler {

  public static final BukkitExceptionAdapter INSTANCE = new BukkitExceptionAdapter();

  public void senderNotPlayer(@NotNull final CommandActor actor,
      @NotNull final SenderNotPlayerException exception) {
    actor.errorLocalized("commands.must-be-player");
  }

  public void senderNotConsole(@NotNull final CommandActor actor,
      @NotNull final SenderNotConsoleException exception) {
    actor.errorLocalized("commands.must-be-console");
  }

  public void invalidPlayer(@NotNull final CommandActor actor,
      @NotNull final InvalidPlayerException exception) {
    actor.errorLocalized("commands.invalid-player", exception.getInput());
  }

  public void invalidWorld(@NotNull final CommandActor actor,
      @NotNull final InvalidWorldException exception) {
    actor.errorLocalized("commands.invalid-world", exception.getInput());
  }

  public void malformedEntitySelector(@NotNull final CommandActor actor,
      @NotNull final MalformedEntitySelectorException exception) {
    actor.errorLocalized("commands.invalid-selector", exception.getInput());
  }

  public void moreThanOnePlayer(@NotNull final CommandActor actor,
      @NotNull final MoreThanOnePlayerException exception) {
    actor.errorLocalized("commands.only-one-player", exception.getInput());
  }

  public void nonPlayerEntities(@NotNull final CommandActor actor,
      @NotNull final NonPlayerEntitiesException exception) {
    actor.errorLocalized("commands.non-players-not-allowed", exception.getInput());
  }
}

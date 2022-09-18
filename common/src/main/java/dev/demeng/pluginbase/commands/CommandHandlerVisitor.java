package dev.demeng.pluginbase.commands;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a <a href="https://en.wikipedia.org/wiki/Visitor_pattern">visitor</a> for a
 * {@link CommandHandler}. This provides a convenient way of performing additional registrations or
 * hooks on a command handler.
 * <p>
 * To accept a visitor, use {@link CommandHandler#accept(CommandHandlerVisitor)}.
 */
public interface CommandHandlerVisitor {

  /**
   * Visits the given command handler
   *
   * @param handler Command handler to visit.
   */
  void visit(@NotNull CommandHandler handler);

}

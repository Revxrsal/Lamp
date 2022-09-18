package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.command.CommandCategory;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.exception.InvalidHelpPageException;
import dev.demeng.pluginbase.commands.help.CommandHelp;
import dev.demeng.pluginbase.commands.help.CommandHelpWriter;
import dev.demeng.pluginbase.commands.process.ContextResolver;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

final class BaseCommandHelp<T> extends ArrayList<T> implements CommandHelp<T> {

  @Override
  public CommandHelp<T> paginate(int page, int elementsPerPage) throws InvalidHelpPageException {
    if (isEmpty()) {
      return new BaseCommandHelp<>();
    }
    BaseCommandHelp<T> list = new BaseCommandHelp<>();
    int size = getPageSize(elementsPerPage);
    if (page > size) {
      throw new InvalidHelpPageException(this, page, elementsPerPage);
    }
    int listIndex = page - 1;
    int l = Math.min(page * elementsPerPage, size());
    for (int i = listIndex * elementsPerPage; i < l; ++i) {
      list.add(get(i));
    }
    return list;
  }

  @Override
  public @Range(from = 1, to = Long.MAX_VALUE) int getPageSize(int elementsPerPage) {
    if (elementsPerPage < 1) {
      throw new IllegalArgumentException(
          "Elements per page cannot be less than 1! (Found " + elementsPerPage + ")");
    }
    return (size() / elementsPerPage) + (size() % elementsPerPage == 0 ? 0 : 1);
  }

  static final class Resolver implements ContextResolver<CommandHelp<?>> {

    private final BaseCommandHandler handler;

    public Resolver(BaseCommandHandler handler) {
      this.handler = handler;
    }

    @Override
    public CommandHelp<?> resolve(@NotNull ContextResolverContext context) {
      if (handler.getHelpWriter() == null) {
        throw new IllegalArgumentException("No help writer is registered!");
      }
      ExecutableCommand helpCommand = context.command();
      CommandHelpWriter<?> writer = handler.getHelpWriter();
      BaseCommandHelp<Object> entries = new BaseCommandHelp<>();
      CommandCategory parent = helpCommand.getParent();
      CommandPath parentPath = parent == null ? null : parent.getPath();
      handler.executables.values().stream().sorted().forEach(c -> {
        if (parentPath == null || parentPath.isParentOf(c.getPath())) {
          if (c != helpCommand) {
            Object generated = writer.generate(c, context.actor());
            if (generated != null) {
              entries.add(generated);
            }
          }
        }
      });
      return entries;
    }
  }
}

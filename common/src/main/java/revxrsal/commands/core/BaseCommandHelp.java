package revxrsal.commands.core;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.InvalidHelpPageException;
import revxrsal.commands.help.CommandHelp;
import revxrsal.commands.help.CommandHelpWriter;
import revxrsal.commands.process.ContextResolver;

import java.util.ArrayList;

final class BaseCommandHelp<T> extends ArrayList<T> implements CommandHelp<T> {

    @Override public CommandHelp<T> paginate(int page, int elementsPerPage) throws InvalidHelpPageException {
        if (isEmpty()) return new BaseCommandHelp<>();
        BaseCommandHelp<T> list = new BaseCommandHelp<>();
        int size = getPageSize(elementsPerPage);
        if (page > size)
            throw new InvalidHelpPageException(this, page);
        int listIndex = page - 1;
        int l = Math.min(page * elementsPerPage, size());
        for (int i = listIndex * elementsPerPage; i < l; ++i) {
            list.add(get(i));
        }
        return list;
    }

    @Override public @Range(from = 1, to = Long.MAX_VALUE) int getPageSize(int elementsPerPage) {
        if (elementsPerPage < 1)
            throw new IllegalArgumentException("Elements per page cannot be less than 1! (Found " + elementsPerPage + ")");
        return (size() / elementsPerPage) + (size() % elementsPerPage == 0 ? 0 : 1);
    }

    static final class Resolver implements ContextResolver<CommandHelp<?>> {

        private final BaseCommandHandler handler;

        public Resolver(BaseCommandHandler handler) {
            this.handler = handler;
        }

        @Override public CommandHelp<?> resolve(@NotNull ContextResolverContext context) {
            if (handler.getHelpWriter() == null)
                throw new IllegalArgumentException("No help writer is registered!");
            ExecutableCommand command = context.command();
            CommandHelpWriter<?> writer = handler.getHelpWriter();
            BaseCommandHelp<Object> entries = new BaseCommandHelp<>();
            CommandCategory parent = context.parameter().getDeclaringCommand().getParent();
            if (parent != null) {
                parent.getCommands().values().forEach(c -> {
                    if (c != command) entries.add(writer.generate(c, context.actor()));
                });
            } else {
                handler.registration.getExecutables().values().forEach(c -> {
                    if (c != command) entries.add(writer.generate(c, context.actor()));
                });
            }
            return entries;
        }
    }

//
//    public static final class Resolver implements ContextResolver<CommandHelp<?>> {
//
//        private final BaseCommandHandler handler;
//
//        public Resolver(BaseCommandHandler handler) {
//            this.handler = handler;
//        }
//
//        @Override public CommandHelp<?> resolve(@NotNull @Unmodifiable List<String> args,
//                                                @NotNull CommandSubject subject,
//                                                @NotNull CommandParameter parameter) throws Throwable {
//            c(handler.helpWriter, "No CommandHelpWriter is registered!");
//            BaseCommandHelp<Object> entries = new BaseCommandHelp<>();
//            HandledCommand command = parameter.getDeclaringCommand().getParent();
//            if (command == null) command = parameter.getDeclaringCommand();
//            for (HandledCommand subcommand : command.getSubcommands().values()) {
//                if (subcommand == parameter.getDeclaringCommand())
//                    continue; // don't include the help command in help menus
//                Object entry = handler.helpWriter.generate(subcommand, subject, args);
//                if (entry != null) {
//                    entries.add(entry);
//                }
//            }
//            return entries;
//        }
//    }
}

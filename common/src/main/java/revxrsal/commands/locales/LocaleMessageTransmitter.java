package revxrsal.commands.locales;

import revxrsal.commands.command.CommandActor;

public interface LocaleMessageTransmitter<T> {
    void reply(CommandActor actor, T message);

    default void error(CommandActor actor, T message) {
        reply(actor, message);
    }

    T format(T message, Object... args);
}

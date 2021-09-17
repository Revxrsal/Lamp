package revxrsal.commands.jda.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.DefaultExceptionHandler;
import revxrsal.commands.exception.InvalidCommandException;

public class JDAExceptionAdapter extends DefaultExceptionHandler {

    public static final JDAExceptionAdapter INSTANCE = new JDAExceptionAdapter();

    @Override protected final void handleUnknown(@NotNull Throwable throwable) {
        if (throwable instanceof GuildOnlyCommandException) guildOnlyCommand((GuildOnlyCommandException) throwable);
        else if (throwable instanceof PrivateMessageOnlyCommandException)
            privateChannelCommandOnly((PrivateMessageOnlyCommandException) throwable);
        else if (throwable instanceof InvalidRoleException) invalidRole((InvalidRoleException) throwable);
        else if (throwable instanceof InvalidChannelException) invalidChannel((InvalidChannelException) throwable);
        else if (throwable instanceof InvalidMemberException) invalidMember((InvalidMemberException) throwable);
        else if (throwable instanceof InvalidCategoryException) invalidCategory((InvalidCategoryException) throwable);
        else if (throwable instanceof InvalidEmoteException) invalidEmote((InvalidEmoteException) throwable);
        else handleUnknownThrowable(throwable);
    }

    // ignore this exception because we can easily get prefix conflict with other bots.
    @Override protected void invalidCommand(@NotNull InvalidCommandException exception) {}

    protected void handleUnknownThrowable(@NotNull Throwable throwable) {throwable.printStackTrace();}

    protected void guildOnlyCommand(@NotNull GuildOnlyCommandException exception) {}

    protected void privateChannelCommandOnly(@NotNull PrivateMessageOnlyCommandException exception) {}

    protected void invalidRole(@NotNull InvalidRoleException exception) {}

    protected void invalidChannel(@NotNull InvalidChannelException exception) {}

    protected void invalidMember(@NotNull InvalidMemberException exception) {}

    protected void invalidCategory(@NotNull InvalidCategoryException exception) {}

    protected void invalidEmote(@NotNull InvalidEmoteException exception) {}

}

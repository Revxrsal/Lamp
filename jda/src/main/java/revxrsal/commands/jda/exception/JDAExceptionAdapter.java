package revxrsal.commands.jda.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.DefaultExceptionHandler;
import revxrsal.commands.exception.InvalidCommandException;

public class JDAExceptionAdapter extends DefaultExceptionHandler {

    public static final JDAExceptionAdapter INSTANCE = new JDAExceptionAdapter();

    @Override protected final void handleUnknown(@NotNull CommandActor actor, @NotNull Throwable throwable) {
        if (throwable instanceof GuildOnlyCommandException)
            guildOnlyCommand(actor, (GuildOnlyCommandException) throwable);
        else if (throwable instanceof PrivateMessageOnlyCommandException)
            privateChannelCommandOnly(actor, (PrivateMessageOnlyCommandException) throwable);
        else if (throwable instanceof InvalidRoleException) invalidRole(actor, (InvalidRoleException) throwable);
        else if (throwable instanceof InvalidChannelException)
            invalidChannel(actor, (InvalidChannelException) throwable);
        else if (throwable instanceof InvalidMemberException) invalidMember(actor, (InvalidMemberException) throwable);
        else if (throwable instanceof InvalidCategoryException)
            invalidCategory(actor, (InvalidCategoryException) throwable);
        else if (throwable instanceof InvalidEmoteException) invalidEmote(actor, (InvalidEmoteException) throwable);
        else handleUnknownThrowable(actor, throwable);
    }

    // ignore this exception because we can easily get prefix conflict with other bots.
    @Override protected void invalidCommand(@NotNull CommandActor actor, @NotNull InvalidCommandException exception) {}

    protected void guildOnlyCommand(@NotNull CommandActor actor, @NotNull GuildOnlyCommandException exception) {
        actor.reply("This command can only be executed in a guild.");
    }

    protected void privateChannelCommandOnly(@NotNull CommandActor actor, @NotNull PrivateMessageOnlyCommandException exception) {
        actor.reply("This command can only be executed in private messages.");
    }

    protected void invalidRole(@NotNull CommandActor actor, @NotNull InvalidRoleException exception) {
        actor.reply("**Invalid role**: " + exception.getInput());
    }

    protected void invalidChannel(@NotNull CommandActor actor, @NotNull InvalidChannelException exception) {
        actor.reply("**Invalid channel**: " + exception.getInput());
    }

    protected void invalidMember(@NotNull CommandActor actor, @NotNull InvalidMemberException exception) {
        actor.reply("**Invalid member**: " + exception.getInput());
    }

    protected void invalidCategory(@NotNull CommandActor actor, @NotNull InvalidCategoryException exception) {
        actor.reply("**Invalid category**: " + exception.getInput());
    }

    protected void invalidEmote(@NotNull CommandActor actor, @NotNull InvalidEmoteException exception) {
        actor.reply("**Invalid emote**: " + exception.getInput());
    }

    protected void handleUnknownThrowable(@NotNull CommandActor actor, @NotNull Throwable throwable) {throwable.printStackTrace();}

}

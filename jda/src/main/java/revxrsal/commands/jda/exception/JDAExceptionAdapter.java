package revxrsal.commands.jda.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.DefaultExceptionHandler;
import revxrsal.commands.exception.InvalidCommandException;

public class JDAExceptionAdapter extends DefaultExceptionHandler {

    public static final JDAExceptionAdapter INSTANCE = new JDAExceptionAdapter();

    // ignore this exception because we can easily get prefix conflict with other bots.
    @Override public void invalidCommand(@NotNull CommandActor actor, @NotNull InvalidCommandException exception) {}

    public void guildOnlyCommand(@NotNull CommandActor actor, @NotNull GuildOnlyCommandException exception) {
        actor.reply("This command can only be executed in a guild.");
    }

    public void privateChannelCommandOnly(@NotNull CommandActor actor, @NotNull PrivateMessageOnlyCommandException exception) {
        actor.reply("This command can only be executed in private messages.");
    }

    public void invalidRole(@NotNull CommandActor actor, @NotNull InvalidRoleException exception) {
        actor.reply("**Invalid role**: " + exception.getInput());
    }

    public void invalidChannel(@NotNull CommandActor actor, @NotNull InvalidChannelException exception) {
        actor.reply("**Invalid channel**: " + exception.getInput());
    }

    public void invalidMember(@NotNull CommandActor actor, @NotNull InvalidMemberException exception) {
        actor.reply("**Invalid member**: " + exception.getInput());
    }

    public void invalidCategory(@NotNull CommandActor actor, @NotNull InvalidCategoryException exception) {
        actor.reply("**Invalid category**: " + exception.getInput());
    }

    public void invalidEmote(@NotNull CommandActor actor, @NotNull InvalidEmoteException exception) {
        actor.reply("**Invalid emote**: " + exception.getInput());
    }
}

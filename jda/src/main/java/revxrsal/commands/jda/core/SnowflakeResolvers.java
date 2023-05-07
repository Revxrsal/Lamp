package revxrsal.commands.jda.core;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;
import revxrsal.commands.jda.JDAActor;
import revxrsal.commands.jda.exception.*;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.util.Strings;

import java.util.List;

/**
 * A utility enum for providing support for general {@link ISnowflake} elements
 * in JDA.
 */
@AllArgsConstructor
enum SnowflakeResolvers implements ValueResolver<ISnowflake> {

    ROLE(Guild::getRoleById, Guild::getRolesByName, InvalidRoleException::new),
    MEMBER(Guild::getMemberById, Guild::getMembersByName, InvalidMemberException::new),
    TEXT_CHANNEL(Guild::getTextChannelById, Guild::getTextChannelsByName, InvalidChannelException::new),
    VOICE_CHANNEL(Guild::getVoiceChannelById, Guild::getVoiceChannelsByName, InvalidChannelException::new),
    STAGE_CHANNEL(Guild::getStageChannelById, Guild::getStageChannelsByName, InvalidChannelException::new),
    EMOTE(Guild::getEmojiById, Guild::getEmojisByName, InvalidEmoteException::new),
    CATEGORY(Guild::getCategoryById, Guild::getCategoriesByName, InvalidCategoryException::new);

    private final GetById getById;
    private final GetByName getByName;
    private final SnowflakeExceptionSupplier exception;

    interface SnowflakeExceptionSupplier {

        InvalidValueException get(CommandParameter parameter, String value);

    }

    private interface GetByName {

        List<?> get(Guild guild, String value, boolean ignoreCase);
    }

    private interface GetById {

        Object get(Guild guild, String id);

    }

    @Override public ISnowflake resolve(@NotNull ValueResolverContext context) {
        String value = context.popForParameter();
        Guild guild = context.actor().as(JDAActor.class).checkInGuild(context.command()).getGuild();
        String snowflake = Strings.getSnowflake(value);
        if (snowflake != null) {
            Object found = getById.get(guild, snowflake);
            if (found == null)
                throw exception.get(context.parameter(), value);
            return (ISnowflake) found;
        } else {
            try {
                return (ISnowflake) getByName.get(guild, value, true).get(0);
            } catch (IndexOutOfBoundsException e) {
                throw exception.get(context.parameter(), value);
            }
        }
    }

    public enum UserResolver implements ValueResolver<User> {
        USER;

        @Override public User resolve(@NotNull ValueResolverContext context) throws Throwable {
            return ((Member) SnowflakeResolvers.MEMBER.resolve(context)).getUser();
        }
    }
}


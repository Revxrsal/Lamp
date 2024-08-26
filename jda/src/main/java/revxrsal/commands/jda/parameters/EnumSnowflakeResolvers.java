package revxrsal.commands.jda.parameters;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.InvalidValueException;
import revxrsal.commands.jda.actor.SlashCommandActor;
import revxrsal.commands.jda.exception.*;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.util.Strings;

import java.util.List;

/**
 * A utility enum for providing support for general {@link ISnowflake} elements
 * in JDA.
 * <p>
 * This enum provides no type-safety. Access through {@link SnowflakeParameterTypes}
 */
enum EnumSnowflakeResolvers implements ParameterType<SlashCommandActor, ISnowflake> {

    ROLE(Guild::getRoleById, Guild::getRolesByName, InvalidRoleException::new),
    MEMBER(Guild::getMemberById, Guild::getMembersByName, InvalidUserException::new),
    TEXT_CHANNEL(Guild::getTextChannelById, Guild::getTextChannelsByName, InvalidChannelException::new),
    VOICE_CHANNEL(Guild::getVoiceChannelById, Guild::getVoiceChannelsByName, InvalidChannelException::new),
    STAGE_CHANNEL(Guild::getStageChannelById, Guild::getStageChannelsByName, InvalidChannelException::new),
    NEWS_CHANNEL(Guild::getNewsChannelById, Guild::getNewsChannelsByName, InvalidChannelException::new),
    THREAD_CHANNEL(Guild::getThreadChannelById, Guild::getThreadChannelsByName, InvalidChannelException::new),
    SCHEDULED_EVENT(Guild::getScheduledEventById, Guild::getScheduledEventsByName, InvalidScheduledEventException::new),
    EMOJI(Guild::getEmojiById, Guild::getEmojisByName, InvalidEmojiException::new),
    CATEGORY(Guild::getCategoryById, Guild::getCategoriesByName, InvalidCategoryException::new);

    private final GetById getById;
    private final GetByName getByName;
    private final SnowflakeExceptionSupplier exception;

    EnumSnowflakeResolvers(GetById getById, GetByName getByName, SnowflakeExceptionSupplier exception) {
        this.getById = getById;
        this.getByName = getByName;
        this.exception = exception;
    }

    @Override
    public ISnowflake parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<SlashCommandActor> context) {
        String value = input.readString();
        Guild guild = context.actor().guild();
        String snowflake = Strings.getSnowflake(value);
        if (snowflake != null) {
            Object found = getById.get(guild, snowflake);
            if (found == null)
                throw exception.get(value);
            return (ISnowflake) found;
        } else {
            try {
                return (ISnowflake) getByName.get(guild, value, true).get(0);
            } catch (IndexOutOfBoundsException e) {
                throw exception.get(value);
            }
        }
    }

    public enum UserResolver implements ParameterType<SlashCommandActor, User> {
        USER;

        @Override
        public User parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<SlashCommandActor> context) {
            return ((Member) EnumSnowflakeResolvers.MEMBER.parse(input, context)).getUser();
        }
    }

    interface SnowflakeExceptionSupplier {

        InvalidValueException get(String value);

    }

    private interface GetByName {

        List<?> get(Guild guild, String value, boolean ignoreCase);
    }

    private interface GetById {

        Object get(Guild guild, String id);

    }
}


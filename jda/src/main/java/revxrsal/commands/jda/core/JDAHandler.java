package revxrsal.commands.jda.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import revxrsal.commands.core.BaseCommandHandler;
import revxrsal.commands.jda.JDAActor;
import revxrsal.commands.jda.JDACommandHandler;
import revxrsal.commands.jda.JDAPermission;
import revxrsal.commands.jda.SlashCommandMapper;
import revxrsal.commands.jda.exception.JDAExceptionAdapter;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ValueResolver;

import static revxrsal.commands.jda.core.SnowflakeResolvers.*;
import static revxrsal.commands.jda.core.SnowflakeResolvers.UserResolver.USER;
import static revxrsal.commands.util.Preconditions.notNull;
import static revxrsal.commands.util.Preconditions.coerceIn;

@ApiStatus.Internal
public final class JDAHandler extends BaseCommandHandler implements JDACommandHandler {
    private final List<SlashCommandMapper> slashCommandMappers = new ArrayList<>();
    private final JDA jda;

    public JDAHandler(@NotNull JDA jda, @NotNull String prefix) {
        super();
        notNull(prefix, "prefix");
        this.jda = notNull(jda, "JDA");
        registerSenderResolver(JDASenderResolver.INSTANCE);
        registerContextResolver(PrivateChannel.class, context -> (PrivateChannel) context.actor().as(JDAActor.class).getChannel());
        registerContextResolver(SelfUser.class, context -> jda.getSelfUser());
        registerContextResolver(MessageReceivedEvent.class, context -> context.actor().as(JDAActor.class).getEvent());
        registerContextResolver(Guild.class, context -> context.actor().as(JDAActor.class).checkInGuild(context.command()).getGuild());
        registerSnowflakeResolver(TextChannel.class, TEXT_CHANNEL);
        registerSnowflakeResolver(VoiceChannel.class, VOICE_CHANNEL);
        registerSnowflakeResolver(StageChannel.class, STAGE_CHANNEL);
        registerSnowflakeResolver(Member.class, MEMBER);
        registerSnowflakeResolver(Emoji.class, EMOTE);
        registerSnowflakeResolver(Role.class, ROLE);
        registerSnowflakeResolver(Category.class, CATEGORY);
        registerSnowflakeResolver(User.class, USER);
        registerDependency(JDA.class, jda);
        registerContextResolver(JDA.class, ContextResolver.of(jda));
        registerResponseHandler(RestAction.class, (response, actor, command) -> response.queue());
        registerResponseHandler(EmbedBuilder.class, (response, actor, command) -> actor.as(JDAActor.class).getChannel().sendMessageEmbeds(response.build()).queue());
        registerResponseHandler(MessageEmbed.class, (response, actor, command) -> actor.as(JDAActor.class).getChannel().sendMessageEmbeds(response).queue());
        setExceptionHandler(JDAExceptionAdapter.INSTANCE);
        registerPermissionReader(JDAPermission::new);
        registerCondition((actor, command, arguments) -> actor.as(JDAActor.class).checkInGuild(command));
        jda.addEventListener(new JDACommandListener(prefix, this));
    }

    @Override
    public @NotNull JDACommandHandler registerSlashCommandMapper(@NotNull SlashCommandMapper commandMapper) {
        notNull(commandMapper, "slash command mapper");
        slashCommandMappers.add(commandMapper);
        return this;
    }

    @Override
    public @NotNull JDACommandHandler registerSlashCommandMapper(int priority, @NotNull SlashCommandMapper commandMapper) {
        notNull(commandMapper, "slash command mapper");
        slashCommandMappers.add(coerceIn(priority, 0, slashCommandMappers.size()), commandMapper);
        return this;
    }

    @Override
    public @NotNull @UnmodifiableView List<SlashCommandMapper> getSlashCommandMappers() {
        return Collections.unmodifiableList(slashCommandMappers);
    }

    private void registerSnowflakeResolver(Class c, ValueResolver res) {
        registerValueResolver(c, res);
    }

    @Override public @NotNull JDA getJDA() {
        return jda;
    }
}

package revxrsal.commands.jda.core;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.core.BaseCommandHandler;
import revxrsal.commands.jda.JDAActor;
import revxrsal.commands.jda.JDACommandHandler;
import revxrsal.commands.jda.JDAPermission;
import revxrsal.commands.jda.exception.JDAExceptionAdapter;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ValueResolver;

import static revxrsal.commands.jda.core.SnowflakeResolvers.*;
import static revxrsal.commands.jda.core.SnowflakeResolvers.UserResolver.USER;
import static revxrsal.commands.util.Preconditions.notNull;

public final class JDAHandler extends BaseCommandHandler implements JDACommandHandler {

    private final JDA jda;

    public JDAHandler(@NotNull JDA jda, @NotNull String prefix) {
        super();
        notNull(prefix, "prefix");
        this.jda = notNull(jda, "JDA");
        registerSenderResolver(JDASenderResolver.INSTANCE);
        registerContextResolver(PrivateChannel.class, (actor, parameter, command) -> (PrivateChannel) actor.as(JDAActor.class).getChannel());
        registerContextResolver(SelfUser.class, (actor, parameter, command) -> jda.getSelfUser());
        registerContextResolver(MessageReceivedEvent.class, (actor, parameter, command) -> actor.as(JDAActor.class).getEvent());
        registerContextResolver(Guild.class, (actor, parameter, command) -> actor.as(JDAActor.class).checkInGuild(command).getGuild());
        registerSnowflakeResolver(TextChannel.class, TEXT_CHANNEL);
        registerSnowflakeResolver(VoiceChannel.class, VOICE_CHANNEL);
        registerSnowflakeResolver(StageChannel.class, STAGE_CHANNEL);
        registerSnowflakeResolver(StoreChannel.class, STORE_CHANNEL);
        registerSnowflakeResolver(Member.class, MEMBER);
        registerSnowflakeResolver(Emote.class, EMOTE);
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
        jda.addEventListener(new JDACommandListener(prefix, this));
    }

    private void registerSnowflakeResolver(Class c, ValueResolver res) {
        registerValueResolver(c, res);
    }

    @Override public @NotNull JDA getJDA() {
        return jda;
    }
}

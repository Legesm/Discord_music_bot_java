package SolevoiSvin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main extends ListenerAdapter {

    private final Map<Long, PlayerManager> musicManagers = new HashMap<>();

    private PlayerManager getPlayerManager(Guild guild) {
        return musicManagers.computeIfAbsent(
                guild.getIdLong(),
                id -> new PlayerManager()
        );
    }

    public static void main(String[] args) throws InterruptedException {
        String token = System.getenv("DISCORD_TOKEN");

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_EXPRESSIONS,
                GatewayIntent.SCHEDULED_EVENTS,
                GatewayIntent.MESSAGE_CONTENT);

        JDA jda = JDABuilder.createDefault(token,intents)
                .setActivity(Activity.playing("Собачка Юрчика"))
                .addEventListeners(new Main())
                .enableCache(CacheFlag.VOICE_STATE)
                .build().awaitReady();

        String guildId = "1456554980696784938";
        Guild guild = jda.getGuildById(guildId);

        assert guild != null;
        jda.updateCommands()
                .addCommands(
                        Commands.slash("ping", "Кто хороший мальчик"),
                        Commands.slash("echo", "Голос").addOption(OptionType.STRING, "text", "Text"),
                        Commands.slash("play", "Вызов")
                                .addOption(OptionType.STRING, "url", "Угадайка", true),
                        Commands.slash("skip", "Тест на iq"),
                        Commands.slash("stop", "В стойло")
                )
                .queue();

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();

        if (message.equalsIgnoreCase("!ping")) {
            event.getChannel().sendMessage("Bimba").queue();

        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "ping":
                handlePing(event);
                break;

            case "echo":
                handleEcho(event);
                break;

            case "play":
                handlePlay(event);
                break;

            case "skip":
                handleSkip(event);
                break;

            case "stop":
                handleStop(event);
                break;

            default:
                event.reply("xd")
                        .setEphemeral(true)
                        .queue();
        }
    }

    private void handlePing(SlashCommandInteractionEvent event) {
        event.reply("Hail Zelenski").queue();
    }

    private void handleEcho(SlashCommandInteractionEvent event) {
        String text = Objects.requireNonNull(event.getOption("text")).getAsString();
        event.reply(text).queue();
    }

    private void handlePlay(SlashCommandInteractionEvent event) {
        if (!isUserInVoice(event)) return;

        event.deferReply().queue();

        Guild guild = event.getGuild();
        Member member = event.getMember();

        if (guild == null || member == null) {
            event.getHook().sendMessage("guild/member").queue();
            return;
        }

        VoiceChannel channel = member.getVoiceState().getChannel().asVoiceChannel();
        PlayerManager manager = getPlayerManager(guild);

        guild.getAudioManager().setSendingHandler(manager.getSendHandler());

        if (!guild.getAudioManager().isConnected()) {
            guild.getAudioManager().openAudioConnection(channel);
        }

        String input = event.getOption("url").getAsString();
        manager.play(input);

        event.getHook().sendMessage("Отрабатываю: " + input).queue();
    }

    private void handleSkip(SlashCommandInteractionEvent event) {
        PlayerManager manager = getPlayerManager(event.getGuild());
        manager.getScheduler().nextTrack();
        event.reply("Next").queue();
    }

    private void handleStop(SlashCommandInteractionEvent event) {
        PlayerManager manager = getPlayerManager(event.getGuild());
        manager.getScheduler().stop();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.reply("Zov Окончен").queue();
    }

    private boolean isUserInVoice(SlashCommandInteractionEvent event) {
        if (event.getMember().getVoiceState().getChannel() == null) {
            event.reply("Zov4")
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        return true;
    }
}
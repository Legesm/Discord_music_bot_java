package SolevoiSvin;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.YoutubeSourceOptions;
import dev.lavalink.youtube.clients.AndroidVr;
import dev.lavalink.youtube.clients.skeleton.Client;

public class PlayerManager {
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;

    public PlayerManager() {
        playerManager = new DefaultAudioPlayerManager();

        YoutubeSourceOptions options = new YoutubeSourceOptions()
                .setAllowSearch(true)
                .setAllowDirectVideoIds(true)
                .setAllowDirectPlaylistIds(true);

        Client[] clients = new Client[]{ new AndroidVr() };
        YoutubeAudioSourceManager ytManager = new YoutubeAudioSourceManager(options, clients);

        playerManager.registerSourceManager(ytManager);

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        player = playerManager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
    }

    public void play(String input) {
        String query = input;

        if (!input.startsWith("http")) {
            query = "ytsearch:" + input;
        }

        playerManager.loadItemOrdered(player, query, scheduler);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }
}

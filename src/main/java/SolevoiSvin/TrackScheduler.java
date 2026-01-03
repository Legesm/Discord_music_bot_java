package SolevoiSvin;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler implements AudioLoadResultHandler, AudioEventListener {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private final int MAX_PLAYLIST_TRACKS = 300;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (player.getPlayingTrack() == null) {
            player.startTrack(track, false);
        } else {
            queue.offer(track);
        }
    }


    @Override
    public void trackLoaded(AudioTrack track) {
        queue(track);
    }


    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        int count = Math.min(playlist.getTracks().size(), MAX_PLAYLIST_TRACKS);
        if (count == 0) return;

        // Добавляем треки в очередь в порядке плейлиста (сначала первый)
        for (int i = 0; i < count; i++) {
            queue(playlist.getTracks().get(i));
        }

        // Если сейчас ничего не играет, запускаем первый трек
        if (player.getPlayingTrack() == null) {
            nextTrack();
        }
    }

    @Override
    public void noMatches() {
        System.out.println("[Audio] No music");
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        System.out.println("[Audio] Error" + exception.getMessage());
    }

    public void nextTrack() {
        AudioTrack next = queue.poll();
        if (next != null) {
            player.startTrack(next, false);
        }
    }

    public void stop() {
        player.stopTrack();
        queue.clear();
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    @Override
    public void onEvent(AudioEvent event) {
        if (event instanceof TrackEndEvent) {
            nextTrack();
        }
    }
}

package SolevoiSvin;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class YtDlpUtil {

    // ОБЯЗАТЕЛЬНО абсолютный путь
    private static final String COOKIES_PATH = "C:/Java/Projects/DiscordMusicBot/src/main/java/main/resources/cookies.txt";
    private static final String YTDLP_PATH = "yt-dlp"; // или полный путь

    public static String getAudioUrl(String youtubeUrl) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    YTDLP_PATH,
                    "--cookies", COOKIES_PATH,
                    "-f", "bestaudio",
                    "-g",
                    youtubeUrl
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line = reader.readLine(); // первая строка = stream URL
            process.waitFor();

            return line;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

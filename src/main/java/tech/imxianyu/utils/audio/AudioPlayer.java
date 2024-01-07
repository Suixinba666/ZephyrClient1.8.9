package tech.imxianyu.utils.audio;

import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;

/**
 * 播放视频里面的音频
 */
public class AudioPlayer {
    public static final int spectrumChannels = 256;
    public MediaPlayer player;
    public Runnable afterPlayed;
    public float[] spectrum = new float[spectrumChannels];

    public final AudioSpectrumListener spectrumListener = (timestamp, duration, magnitudes, phases) -> {
        //Array Copy
        System.arraycopy(magnitudes, 0, spectrum, 0, magnitudes.length);
    };
    @Getter
    private float volume = 0.25f;
    
    private int getThreshold() {
        return -90;
    }

    public AudioPlayer(Media media) {


        this.player = new MediaPlayer(media);
        this.player.setVolume(volume);

        this.player.setAudioSpectrumInterval(0.05);
        this.player.setAudioSpectrumThreshold(this.getThreshold());
        this.player.setAudioSpectrumNumBands(spectrumChannels);
        this.player.setAudioSpectrumListener(this.spectrumListener);
        this.player.setOnEndOfMedia(() -> {
            int curCount = player.getCurrentCount();
            int cycCount = player.getCycleCount();
            if (cycCount != MediaPlayer.INDEFINITE && curCount >= cycCount)
                player.stop();
        });
    }

    @SneakyThrows
    public AudioPlayer(String musicPath) {
        this(new Media(musicPath));
    }


    @SneakyThrows
    public AudioPlayer(File file) {
        this(new Media(file.toURI().toString()));
    }

    @SneakyThrows
    public void setAudio(String musicPath) {
        if (!this.isFinished()) {
            this.close();
        }

        this.player = new MediaPlayer(new Media(musicPath));
        this.player.setVolume(volume);

        this.player.setAudioSpectrumInterval(0.05);
        this.player.setAudioSpectrumThreshold(this.getThreshold());
        this.player.setAudioSpectrumNumBands(spectrumChannels);
        this.player.setAudioSpectrumListener(this.spectrumListener);
        this.player.setOnEndOfMedia(() -> {
            int curCount = player.getCurrentCount();
            int cycCount = player.getCycleCount();
            if (cycCount != MediaPlayer.INDEFINITE && curCount >= cycCount)
                player.stop();
        });
    }

    @SneakyThrows
    public void setAudio(File file) {
        if (!this.isFinished()) {
            this.close();
        }

        this.player = new MediaPlayer(new Media(file.toURI().toString()));
        this.player.setVolume(volume);

        this.player.setAudioSpectrumInterval(0.05);
        this.player.setAudioSpectrumThreshold(this.getThreshold());
        this.player.setAudioSpectrumNumBands(spectrumChannels);
        this.player.setAudioSpectrumListener(this.spectrumListener);
        this.player.setOnEndOfMedia(() -> {
            int curCount = player.getCurrentCount();
            int cycCount = player.getCycleCount();
            if (cycCount != MediaPlayer.INDEFINITE && curCount >= cycCount)
                player.stop();
        });
    }

    public void play() {
        try {
            this.player.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param progress 毫秒
     */
    @SneakyThrows
    public void setProgress(long progress) {

        this.player.seek(Duration.millis((progress + 1) * 1000));
    }

    @SneakyThrows
    public void close() {
        player.stop();

        if (afterPlayed != null) {
            afterPlayed.run();
            afterPlayed = null;
        }
    }

    public void setAfterPlayed(Runnable runnable) {
        this.afterPlayed = runnable;
    }

    public int getTotalTimeSeconds() {
        return (int) this.player.getMedia().getDuration().toSeconds();
    }

    public int getCurrentTimeSeconds() {
        return (int) this.player.getCurrentTime().toSeconds();
    }

    public int getTotalTimeMillis() {
        return (int) this.player.getMedia().getDuration().toMillis();
    }

    public int getCurrentTimeMillis() {
        return (int) this.player.getCurrentTime().toMillis();
    }

    public boolean isFinished() {
        return this.player.getStatus() == MediaPlayer.Status.STOPPED;
    }

    public boolean isPausing() {
        return this.player.getStatus() == MediaPlayer.Status.PAUSED;
    }

    public void setVolume(float volume) {
        this.volume = volume;
        this.player.setVolume(this.getVolume());
    }

    public void pause() {
        this.player.pause();
    }

    public void unpause() {
        this.player.play();
    }

}
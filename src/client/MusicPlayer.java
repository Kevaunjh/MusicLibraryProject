package client;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

public class MusicPlayer {
    private MediaPlayer player;
    private MediaPlayer.Status currentStatus = MediaPlayer.Status.UNKNOWN;
    private boolean isPlayQueued = false;

    public void play() {
        if (player != null) {
            if (currentStatus == MediaPlayer.Status.READY) {
                System.out.println("Playing song...");
                player.play();
                isPlayQueued = false;
            } else if (currentStatus == MediaPlayer.Status.PAUSED || currentStatus == MediaPlayer.Status.STOPPED) {
                System.out.println("Resuming playback...");
                player.play();
            } else if (currentStatus == MediaPlayer.Status.PLAYING) {
                System.out.println("The song is already playing.");
            } else if (currentStatus == MediaPlayer.Status.UNKNOWN) {
                if (!isPlayQueued) {
                    System.out.println("MediaPlayer is not ready. Waiting for readiness...");
                    isPlayQueued = true;
                    player.setOnReady(() -> {
                        System.out.println("MediaPlayer is ready. Starting playback...");
                        play();
                    });
                }
            } else {
                System.out.println("Cannot play. Current status: " + currentStatus);
            }
        } else {
            System.out.println("No song loaded to play.");
        }
    }

    public void pause() {
        if (player != null && currentStatus == MediaPlayer.Status.PLAYING) {
            System.out.println("Pausing song at: " + player.getCurrentTime().toSeconds() + " seconds.");
            player.pause();
        } else {
            System.out.println("No song is currently playing to pause.");
        }
    }

    public void stop() {
        if (player != null) {
            System.out.println("Stopping song and resetting playback...");
            player.stop();
            player.seek(Duration.ZERO);
            System.out.println("Playback position reset to the beginning.");
        } else {
            System.out.println("No song is currently playing to stop.");
        }
    }

    public void seek(Duration time) {
        if (player != null) {
            System.out.println("Seeking to: " + time.toSeconds() + " seconds.");
            player.seek(time);
        } else {
            System.out.println("No song is loaded to seek.");
        }
    }

    public void loadSong(String filePath) {
        try {
            File file = new File(filePath);
            String canonicalPath = file.getCanonicalPath();
            System.out.println("Loading file: " + canonicalPath);

            // Prevent reloading the same file
            if (player != null && player.getMedia() != null) {
                String currentSource = new File(player.getMedia().getSource().replace("file:/", "").replace("%20", " "))
                        .getCanonicalPath();
                if (canonicalPath.equals(currentSource)) {
                    System.out.println("The same file is already loaded. No need to reload.");
                    return;
                }
            }

            if (player != null) {
                System.out.println("Stopping and releasing the current song...");
                player.stop();
                player.dispose();
            }

            Media media = new Media(file.toURI().toString());
            player = new MediaPlayer(media);

            player.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                currentStatus = newStatus;
                System.out.println("MediaPlayer status updated to: " + currentStatus);
            });

            System.out.println("Initial MediaPlayer status: " + player.getStatus());

            isPlayQueued = false;

            player.setOnReady(() -> {
                System.out.println("Song is ready for playback.");
                if (isPlayQueued) {
                    play();
                }
            });

            player.setOnError(() -> {
                System.err.println("MediaPlayer Error: " + player.getError().getMessage());
                System.err.println("Ensure the file format is supported and the path is correct.");
            });

            player.setOnEndOfMedia(() -> System.out.println("Song playback finished."));
        } catch (Exception e) {
            System.err.println("Error loading song: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isPlaying() {
        return currentStatus == MediaPlayer.Status.PLAYING;
    }

    public MediaPlayer.Status getStatus() {
        return currentStatus;
    }

    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        if (player == null) {
            System.err.println("Error: MediaPlayer is not initialized.");
            return null;
        }
        return player.currentTimeProperty();
    }

    public Duration getTotalDuration() {
        return player != null ? player.getTotalDuration() : Duration.UNKNOWN;
    }

    public void dispose() {
        if (player != null) {
            System.out.println("Disposing MediaPlayer resources...");
            player.dispose();
            player = null;
        }
    }

    public MediaPlayer getMediaPlayer() {
        return player;
    }
}

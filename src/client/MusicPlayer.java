package client;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;

public class MusicPlayer {
    private MediaPlayer player;
    private MediaPlayer.Status currentStatus = MediaPlayer.Status.UNKNOWN; // Global status

    // Play the song
    public void play() {
        if (player != null) {
            switch (currentStatus) {
                case READY:
                    System.out.println("Playing song from the beginning...");
                    player.seek(Duration.ZERO); // Start from the beginning
                    player.play();
                    break;
                case PAUSED:
                    System.out.println("Resuming song from current position: " + player.getCurrentTime().toSeconds()
                            + " seconds.");
                    player.play();
                    break;
                case PLAYING:
                    System.out.println("The song is already playing.");
                    break;
                case STOPPED:
                    System.out.println("Restarting song from the beginning...");
                    player.seek(Duration.ZERO); // Reset to the beginning before playing
                    player.play();
                    break;
                case UNKNOWN:
                    System.out.println("MediaPlayer status is UNKNOWN. Waiting for the song to be ready...");
                    player.setOnReady(() -> {
                        System.out.println("Song is now ready. Playing...");
                        player.play();
                    });
                    break;
                case DISPOSED:
                    System.out.println("Cannot play. The MediaPlayer is disposed. Please reload the song.");
                    break;
                default:
                    System.out.println("Cannot play. Current status: " + currentStatus);
            }
        } else {
            System.out.println("No song loaded to play.");
        }
    }

    // Pause the song
    public void pause() {
        if (player != null && currentStatus == MediaPlayer.Status.PLAYING) {
            System.out.println("Pausing song at: " + player.getCurrentTime().toSeconds() + " seconds.");
            player.pause();
        } else {
            System.out.println("No song is currently playing to pause.");
        }
    }

    // Stop the song
    public void stop() {
        if (player != null) {
            System.out.println("Stopping song and resetting playback...");
            player.stop(); // Stops playback and resets the position to the beginning
            player.seek(Duration.ZERO); // Explicitly seek to the start
            System.out.println("Playback position reset to the beginning.");
        } else {
            System.out.println("No song is currently playing to stop.");
        }
    }

    // Seek to a specific point in the song
    public void seek(Duration time) {
        if (player != null) {
            System.out.println("Seeking to: " + time.toSeconds() + " seconds.");
            player.seek(time);
        } else {
            System.out.println("No song is loaded to seek.");
        }
    }

    // Load a new song for playback
    public void loadSong(String filePath) {
        try {
            File file = new File(filePath);
            String canonicalPath = file.getCanonicalPath(); // Normalize path
            System.out.println("Loading file: " + canonicalPath);

            // Prevent reloading the same file
            if (player != null && player.getMedia() != null) {
                String currentSource = new File(player.getMedia().getSource().replace("file:/", "").replace("%20", " "))
                        .getCanonicalPath();
                if (canonicalPath.equals(currentSource)) {
                    System.out.println("The same file is already loaded. No need to reload.");
                    return; // Skip reloading
                }
            }

            // Stop and release the current player if a new file is loaded
            if (player != null) {
                System.out.println("Stopping and releasing the current song...");
                player.stop();
                player.dispose();
            }

            Media media = new Media(file.toURI().toString());
            player = new MediaPlayer(media);

            // Update the global status whenever it changes
            player.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                currentStatus = newStatus;
                System.out.println("MediaPlayer status updated to: " + currentStatus);
            });

            System.out.println("Initial MediaPlayer status: " + player.getStatus());

            // Start playback automatically when the song is ready
            player.setOnReady(() -> {
                System.out.println("Song loaded successfully and ready to play.");
                play(); // Automatically start playback
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

    // Check if the song is currently playing
    public boolean isPlaying() {
        return currentStatus == MediaPlayer.Status.PLAYING;
    }

    // Get the current MediaPlayer status
    public MediaPlayer.Status getStatus() {
        return currentStatus;
    }
}

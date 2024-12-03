package client;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

public class MusicPlayer {
    private MediaPlayer player; // MediaPlayer instance for playing audio
    private MediaPlayer.Status currentStatus = MediaPlayer.Status.UNKNOWN; // Track the current status of the MediaPlayer
    private boolean isPlayQueued = false; // Flag to indicate if a play request is queued

    /**
     * Plays the currently loaded song.
     * Handles different states to ensure appropriate playback actions.
     */
    public void play() {
        if (player != null) {
            if (currentStatus == MediaPlayer.Status.READY) {
                // Play the song if the player is ready
                System.out.println("Playing song...");
                player.play();
                isPlayQueued = false;
            } else if (currentStatus == MediaPlayer.Status.PAUSED || currentStatus == MediaPlayer.Status.STOPPED) {
                // Resume playback if the player was paused or stopped
                System.out.println("Resuming playback...");
                player.play();
            } else if (currentStatus == MediaPlayer.Status.PLAYING) {
                // Notify if the song is already playing
                System.out.println("The song is already playing.");
            } else if (currentStatus == MediaPlayer.Status.UNKNOWN) {
                // Handle the case when the player is not ready
                if (!isPlayQueued) {
                    System.out.println("MediaPlayer is not ready. Waiting for readiness...");
                    isPlayQueued = true;
                    player.setOnReady(() -> {
                        // Play the song when the MediaPlayer is ready
                        System.out.println("MediaPlayer is ready. Starting playback...");
                        play();
                    });
                }
            } else {
                // Handle other undefined statuses
                System.out.println("Cannot play. Current status: " + currentStatus);
            }
        } else {
            // Notify if no song is loaded
            System.out.println("No song loaded to play.");
        }
    }

    /**
     * Pauses the currently playing song.
     * Logs the current time for resuming playback later.
     */
    public void pause() {
        if (player != null && currentStatus == MediaPlayer.Status.PLAYING) {
            // Pause playback if a song is playing
            System.out.println("Pausing song at: " + player.getCurrentTime().toSeconds() + " seconds.");
            player.pause();
        } else {
            // Notify if no song is currently playing
            System.out.println("No song is currently playing to pause.");
        }
    }

    /**
     * Stops the currently playing song and resets its playback position to the beginning.
     */
    public void stop() {
        if (player != null) {
            // Stop the song and reset playback position
            System.out.println("Stopping song and resetting playback...");
            player.stop();
            player.seek(Duration.ZERO);
            System.out.println("Playback position reset to the beginning.");
        } else {
            // Notify if no song is currently playing
            System.out.println("No song is currently playing to stop.");
        }
    }

    public void seek(Duration time) {
        if (player != null) {
            // Seek to the specified time in the song
            System.out.println("Seeking to: " + time.toSeconds() + " seconds.");
            player.seek(time);
        } else {
            // Notify if no song is loaded
            System.out.println("No song is loaded to seek.");
        }
    }


    public void loadSong(String filePath) {
        try {
            // Create a File object for the given path and get its canonical path
            File file = new File(filePath);
            String canonicalPath = file.getCanonicalPath();
            System.out.println("Loading file: " + canonicalPath);

            // Prevent reloading the same file to avoid redundant operations
            if (player != null && player.getMedia() != null) {
                String currentSource = new File(player.getMedia().getSource().replace("file:/", "").replace("%20", " "))
                        .getCanonicalPath();
                if (canonicalPath.equals(currentSource)) {
                    System.out.println("The same file is already loaded. No need to reload.");
                    return;
                }
            }

            // Dispose of the current player if it exists before creating a new one
            if (player != null) {
                System.out.println("Stopping and releasing the current song...");
                player.stop();
                player.dispose();
            }

            // Create a new Media object and MediaPlayer instance
            Media media = new Media(file.toURI().toString());
            player = new MediaPlayer(media);

            // Add listener for status changes
            player.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                currentStatus = newStatus;
                System.out.println("MediaPlayer status updated to: " + currentStatus);
            });

            System.out.println("Initial MediaPlayer status: " + player.getStatus());

            isPlayQueued = false; // Reset the play queue flag

            // Set a callback for when the player is ready
            player.setOnReady(() -> {
                System.out.println("Song is ready for playback.");
                if (isPlayQueued) {
                    play();
                }
            });

            // Set an error callback for handling errors during media playback
            player.setOnError(() -> {
                System.err.println("MediaPlayer Error: " + player.getError().getMessage());
                System.err.println("Ensure the file format is supported and the path is correct.");
            });

            // Set a callback for when the media reaches its end
            player.setOnEndOfMedia(() -> System.out.println("Song playback finished."));
        } catch (Exception e) {
            // Handle potential errors during song loading
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
            // Notify if the player is not initialized
            System.err.println("Error: MediaPlayer is not initialized.");
            return null;
        }
        return player.currentTimeProperty();
    }

    public Duration getTotalDuration() {
        return player != null ? player.getTotalDuration() : Duration.UNKNOWN;
    }

    /**
     * Disposes of the MediaPlayer and releases its resources.
     */
    public void dispose() {
        if (player != null) {
            // Release resources when disposing of the MediaPlayer
            System.out.println("Disposing MediaPlayer resources...");
            player.dispose();
            player = null;
        }
    }

    public MediaPlayer getMediaPlayer() {
        return player;
    }
}

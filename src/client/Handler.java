import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class Handler {
    private final MusicPlayer player = new MusicPlayer();
    private final P2PProtocol protocol = new P2PProtocol();

    public MusicPlayer getPlayer() {
        return player;
    }

    public void loadSongs(ListView<String> songList) {
        File downloadsDir = new File("../../Downloads");
        if (downloadsDir.exists() && downloadsDir.isDirectory()) {
            File[] files = downloadsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".mp3")) {
                        songList.getItems().add(file.getName());
                    }
                }
            }
        }
    }

    private boolean isFileAlreadyInDownloads(String fileName, Path downloadsDirectory) {
        File targetFile = new File(downloadsDirectory.toFile(), fileName);
        return targetFile.exists();
    }

    public void downloadSelectedSong(ListView<String> songList, Path currentClientDownloadsDirectory,
            ObservableList<String> downloadedSongs) {
        String selectedSong = songList.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            // Check if the song is already in My Downloads
            if (downloadedSongs.contains(selectedSong)
                    || isFileAlreadyInDownloads(selectedSong, currentClientDownloadsDirectory)) {
                showError("Duplicate Download", "This song is already in My Downloads.");
                return;
            }

            String sourcePath = "../../Songs/" + selectedSong;
            protocol.downloadFile(null, sourcePath, currentClientDownloadsDirectory.toString());
            File downloadedFile = new File(currentClientDownloadsDirectory.toFile(), selectedSong);
            if (downloadedFile.exists()) {
                downloadedSongs.add(selectedSong);
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Download Successful");
                successAlert.setContentText("File downloaded to My Downloads: " + selectedSong);
                successAlert.showAndWait();
            } else {
                showError("Download Failed", "The file could not be downloaded.");
            }
        } else {
            showError("No Song Selected", "Please select a song to download.");
        }
    }

    public void loadSongs(ObservableList<String> songList) {
        File downloadsDir = new File("../../Downloads");
        if (downloadsDir.exists() && downloadsDir.isDirectory()) {
            File[] files = downloadsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".mp3")) {
                        songList.add(file.getName());
                    }
                }
            }
        }
    }

    public void downloadFromCommunityPlaylist(String songName, Path communityPlaylistDirectory,
            Path currentClientDownloadsDirectory, ObservableList<String> downloadedSongs) {
        if (songName != null) {
            // Check if the song is already in My Downloads
            if (downloadedSongs.contains(songName)
                    || isFileAlreadyInDownloads(songName, currentClientDownloadsDirectory)) {
                showError("Duplicate Download", "This song is already in My Downloads.");
                return;
            }

            File sourceFile = new File(communityPlaylistDirectory.toFile(), songName);
            File targetFile = new File(currentClientDownloadsDirectory.toFile(), songName);
            try {
                Files.copy(sourceFile.toPath(), targetFile.toPath());
                downloadedSongs.add(songName);
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Download Successful");
                successAlert.setContentText(songName + " downloaded to My Downloads.");
                successAlert.showAndWait();
            } catch (IOException e) {
                showError("Download Failed", "Failed to download the song from the Community Playlist.");
            }
        } else {
            showError("No Song Selected", "Please select a song to download.");
        }
    }

    public void downloadFromPeer(String peerAddress, Path currentClientDownloadsDirectory,
            ObservableList<String> downloadedSongs) {
        if (peerAddress != null) {
            String[] peerSongs = { "song1.mp3", "song2.mp3" }; // Mock data, replace with actual peer songs
            for (String song : peerSongs) {
                if (!downloadedSongs.contains(song)
                        && !isFileAlreadyInDownloads(song, currentClientDownloadsDirectory)) {
                    protocol.downloadFile(new InetSocketAddress(peerAddress, 5000), song,
                            currentClientDownloadsDirectory.toString());
                    downloadedSongs.add(song);
                }
            }
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Download Successful");
            successAlert.setContentText("All songs from peer downloaded.");
            successAlert.showAndWait();
        } else {
            showError("No Peer Selected", "Please select a peer to download from.");
        }
    }

    public void addToCommunityPlaylist(String songName, Path currentClientDownloadsDirectory,
            Path communityPlaylistDirectory, ObservableList<String> communityPlaylist) {
        if (songName != null) {
            if (communityPlaylist.contains(songName)) {
                showError("Duplicate Song", "This song is already in the Community Playlist.");
                return;
            }

            File sourceFile = new File(currentClientDownloadsDirectory.toString(), songName);
            File targetFile = new File(communityPlaylistDirectory.toFile(), songName);
            try {
                Files.copy(sourceFile.toPath(), targetFile.toPath());
                communityPlaylist.add(songName);
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Playlist Updated");
                successAlert.setContentText(songName + " added to the Community Playlist.");
                successAlert.showAndWait();
            } catch (IOException e) {
                showError("Error Adding to Playlist", "Failed to add song to the Community Playlist.");
            }
        } else {
            showError("No Song Selected", "Please select a song to add to the playlist.");
        }
    }

    public void removeFromCommunityPlaylist(String songName, Path communityPlaylistDirectory,
            ObservableList<String> communityPlaylist) {
        if (songName != null) {
            File targetFile = new File(communityPlaylistDirectory.toFile(), songName);
            if (targetFile.exists()) {
                targetFile.delete();
                communityPlaylist.remove(songName);
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Playlist Updated");
                successAlert.setContentText(songName + " removed from the Community Playlist.");
                successAlert.showAndWait();
            } else {
                showError("Error Removing Song", "The selected song does not exist in the Community Playlist.");
            }
        } else {
            showError("No Song Selected", "Please select a song to remove from the playlist.");
        }
    }

    public void playSelectedDownloadedSong(ListView<String> downloadsList, Path currentClientDownloadsDirectory) {
        String selectedSong = downloadsList.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            String filePath = currentClientDownloadsDirectory.resolve(selectedSong).toString();
            File songFile = new File(filePath);
            if (songFile.exists()) {
                player.loadSong(filePath);
                player.play();
            } else {
                showError("File Not Found", "The selected song file does not exist in My Downloads.");
            }
        } else {
            showError("No Song Selected", "Please select a song to play.");
        }
    }

    public void refreshPeerList(ObservableList<String> connectedPeers) {
        connectedPeers.clear();
        for (InetSocketAddress peer : protocol.getPeers()) {
            connectedPeers.add(peer.toString());
        }
    }

    public void loadCommunityPlaylist(Path communityPlaylistDirectory, ObservableList<String> communityPlaylist) {
        File communityDir = communityPlaylistDirectory.toFile();
        if (communityDir.exists() && communityDir.isDirectory()) {
            File[] files = communityDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".mp3")) {
                        communityPlaylist.add(file.getName());
                    }
                }
            }
        }
    }

    // Play selected song from the provided ListView
    public void playSelectedSong(ListView<String> songList) {
        String selectedSong = songList.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            String filePath = "../../Downloads/" + selectedSong;
            File songFile = new File(filePath);
            if (songFile.exists()) {
                player.loadSong(filePath);
                player.play();
            } else {
                showError("File Not Found", "The selected song file does not exist.");
            }
        } else {
            showError("No Song Selected", "Please select a song to play.");
        }
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

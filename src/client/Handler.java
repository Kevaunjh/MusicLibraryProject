package client;

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

    private boolean isFileAlreadyInDownloads(String fileName, Path downloadsDirectory) {
        File targetFile = new File(downloadsDirectory.toFile(), fileName);
        return targetFile.exists();
    }

    public void downloadSelectedSong(ListView<String> songList, Path currentClientDownloadsDirectory,
            ObservableList<String> downloadedSongs) {
        if (currentClientDownloadsDirectory == null) {
            showError("Download Directory Not Set", "The download directory is not initialized.");
            return;
        }

        String selectedSong = songList.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            if (downloadedSongs.contains(selectedSong)
                    || isFileAlreadyInDownloads(selectedSong, currentClientDownloadsDirectory)) {
                showError("Duplicate Download", "This song is already in My Downloads.");
                return;
            }

            File sourceFile = new File("../Songs", selectedSong);
            File targetFile = new File(currentClientDownloadsDirectory.toFile(), selectedSong);

            try {
                Files.copy(sourceFile.toPath(), targetFile.toPath());
                downloadedSongs.add(selectedSong);

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Download Successful");
                successAlert.setHeaderText(null);
                successAlert.setContentText("File downloaded to My Downloads: " + selectedSong);
                successAlert.showAndWait();
            } catch (Exception e) {
                showError("Download Failed", "The file could not be downloaded: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("No Song Selected", "Please select a song to download.");
        }
    }

    public void loadSongs(ObservableList<String> songList) {
        File downloadsDir = new File("../Downloads");
        if (downloadsDir.exists() && downloadsDir.isDirectory()) {
            File[] files = downloadsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".mp3")) {
                        songList.add(file.getName());
                        System.out.println("Loaded song: " + file.getName());
                    }
                }
            } else {
                System.out.println("No files found in the directory.");
            }
        } else {
            System.out.println("Directory does not exist: " + downloadsDir.getAbsolutePath());
        }
    }

    public void downloadFromCommunityPlaylist(String songName, Path communityPlaylistDirectory,
            Path currentClientDownloadsDirectory, ObservableList<String> downloadedSongs) {
        if (songName != null) {
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

    public void downloadFromPeer(String peerName, Path downloadsDirectory, ObservableList<String> downloadedSongs) {
        try {
            System.out.println("Downloading from peer: " + peerName);

            Path peerDirectory = Path.of("./client", "client/" + peerName);
            System.out.println("Peer directory path: " + peerDirectory);

            if (!Files.exists(peerDirectory) || !Files.isDirectory(peerDirectory)) {
                System.out.println("Peer directory does not exist: " + peerDirectory);
                throw new IllegalArgumentException("The peer directory does not exist: " + peerDirectory);
            }

            Files.list(peerDirectory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".mp3"))
                    .forEach(file -> {
                        try {
                            String fileName = file.getFileName().toString();

                            if (downloadedSongs.contains(fileName) ||
                                    Files.exists(downloadsDirectory.resolve(fileName))) {
                                System.out.println("File already exists in downloads: " + fileName);
                                return;
                            }

                            Path targetFile = downloadsDirectory.resolve(fileName);

                            Files.copy(file, targetFile);
                            downloadedSongs.add(fileName);
                            System.out.println("Downloaded: " + fileName);
                        } catch (Exception e) {
                            System.err.println("Error downloading file: " + file.getFileName());
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error downloading from peer: " + e.getMessage());
            e.printStackTrace();
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


    public void playSelectedSong(ListView<String> songList) {
        String selectedSong = songList.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            String filePath = "../Downloads/" + selectedSong;
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

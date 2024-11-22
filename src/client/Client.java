package client;

import common.Library;
import common.PeerInfo;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;

public class Client extends Application {
    private static Library musicLibrary;
    private static ObservableList<String> connectedPeers = FXCollections.observableArrayList();
    private static PeerInfoImpl peerInfo;
    private static Path personalDirectory;
    private static String peerAddress;

    public static void main(String[] args) {
        try {
            musicLibrary = (Library) Naming.lookup("//localhost/Library");

            peerAddress = "Client_" + System.currentTimeMillis();
            personalDirectory = Path.of("./client", "client/" + peerAddress);
            Files.createDirectories(personalDirectory);
            System.out.println("Personal directory created at: " + personalDirectory);

            List<String> playlist = getFilesFromPersonalDirectory();
            peerInfo = new PeerInfoImpl(peerAddress, playlist);
            musicLibrary.registerPeer(peerInfo);
            System.out.println("Client registered with name: " + peerAddress);
            launch(args);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        MusicLibraryGUI gui = new MusicLibraryGUI(musicLibrary, connectedPeers, personalDirectory);
        gui.start(primaryStage);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (musicLibrary != null && peerInfo != null) {
                    musicLibrary.deregisterPeer(peerInfo);
                    System.out.println("Client deregistered from the server.");
                }
            } catch (Exception e) {
                System.err.println("Error during deregistration: " + e.getMessage());
            }
        }));
    }

    private static List<String> getFilesFromPersonalDirectory() {
        List<String> playlist = new ArrayList<>();
        try {
            if (Files.exists(personalDirectory)) {
                Files.list(personalDirectory)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".mp3"))
                        .forEach(path -> playlist.add(path.getFileName().toString()));
            }
        } catch (Exception e) {
            System.err.println("Error reading personal directory: " + e.getMessage());
        }
        return playlist;
    }

    public static void refreshPeers() {
        try {
            List<String> peers = musicLibrary.getConnectedPeers();
            connectedPeers.setAll(peers);
            System.out.println("Connected peers refreshed: " + peers);
        } catch (Exception e) {
            System.err.println("Error refreshing peers: " + e.getMessage());
        }
    }

    public static void downloadFromPeer(String peerAddress, String peerFolderPath, Path localDownloadsDirectory) {
        try {
            Path peerDirectory = Path.of(peerFolderPath);

            if (Files.exists(peerDirectory) && Files.isDirectory(peerDirectory)) {
                Files.list(peerDirectory)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".mp3"))
                        .forEach(path -> {
                            try {
                                Path targetFile = localDownloadsDirectory.resolve(path.getFileName());
                                Files.copy(path, targetFile);

                                System.out.println("File downloaded from peer: " + path.getFileName());
                            } catch (Exception e) {
                                System.err.println("Error downloading file from peer: " + e.getMessage());
                            }
                        });
            } else {
                System.err.println("Peer directory does not exist or is invalid: " + peerDirectory);
            }
        } catch (Exception e) {
            System.err.println("Error downloading from peer: " + e.getMessage());
        }
    }
}

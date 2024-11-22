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
    private static Library musicLibrary; // RMI interface to the server
    private static ObservableList<String> connectedPeers = FXCollections.observableArrayList(); // List for dynamic peer
                                                                                                // updates
    private static PeerInfoImpl peerInfo; // Client's peer information
    private static Path personalDirectory; // Unique personal directory for the client
    private static String peerAddress; // Unique client name

    public static void main(String[] args) {
        try {
            // Connect to the RMI server
            musicLibrary = (Library) Naming.lookup("//localhost/Library");

            // Generate a unique client name
            peerAddress = "Client_" + System.currentTimeMillis();

            // Ensure the personal directory is unique and uses the peer name
            personalDirectory = Path.of("./client", "client/" + peerAddress);
            Files.createDirectories(personalDirectory);
            System.out.println("Personal directory created at: " + personalDirectory);

            // Get files from the client's directory to populate the playlist
            List<String> playlist = getFilesFromPersonalDirectory();

            // Initialize this client's peer information
            peerInfo = new PeerInfoImpl(peerAddress, playlist);

            // Register this client with the server
            musicLibrary.registerPeer(peerInfo);
            System.out.println("Client registered with name: " + peerAddress);

            // Launch the GUI
            launch(args);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Launch the GUI with dynamic peer updates and shared directory
        MusicLibraryGUI gui = new MusicLibraryGUI(musicLibrary, connectedPeers, personalDirectory); // Pass
                                                                                                    // personalDirectory
        gui.start(primaryStage);

        // Add a shutdown hook to deregister the client on exit
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
            // Fetch the list of connected peers from the server
            List<String> peers = musicLibrary.getConnectedPeers();

            // Update the observable list for the GUI
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

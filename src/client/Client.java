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
    // Reference to the remote Library object for communication
    private static Library musicLibrary;
    
    // List of connected peers to be displayed in the UI
    private static ObservableList<String> connectedPeers = FXCollections.observableArrayList();
    
    // Instance representing the client's information (peer)
    private static PeerInfoImpl peerInfo;
    
    // Path to the client's personal directory for storing music files
    private static Path personalDirectory;
    
    // Unique address for the client
    private static String peerAddress;

    // Main method for client initialization and launching the JavaFX application
    public static void main(String[] args) {
        try {
            // Look up the remote Library object on localhost using RMI
            musicLibrary = (Library) Naming.lookup("//localhost/Library");

            // Generate a unique peer address using the current timestamp
            peerAddress = "Client_" + System.currentTimeMillis();

            // Set up the path for the client's personal directory
            personalDirectory = Path.of("./client", "client/" + peerAddress);
            Files.createDirectories(personalDirectory); // Create the directory if it doesn't exist
            System.out.println("Personal directory created at: " + personalDirectory);

            // Retrieve the list of files (songs) from the personal directory
            List<String> playlist = getFilesFromPersonalDirectory();

            // Create the PeerInfo object and register with the Library service
            peerInfo = new PeerInfoImpl(peerAddress, playlist);
            musicLibrary.registerPeer(peerInfo);
            System.out.println("Client registered with name: " + peerAddress);

            // Launch the JavaFX application
            launch(args);
        } catch (Exception e) {
            // Print any exceptions that occur during client setup
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Start method for initializing and displaying the GUI
    @Override
    public void start(Stage primaryStage) {
        // Create and start the MusicLibraryGUI
        MusicLibraryGUI gui = new MusicLibraryGUI(musicLibrary, connectedPeers, personalDirectory);
        gui.start(primaryStage);

        // Add a shutdown hook to deregister the client when the application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (musicLibrary != null && peerInfo != null) {
                    musicLibrary.deregisterPeer(peerInfo);
                    System.out.println("Client deregistered from the server.");
                }
            } catch (Exception e) {
                // Print any exceptions that occur during deregistration
                System.err.println("Error during deregistration: " + e.getMessage());
            }
        }));
    }

    // Method to get a list of .mp3 files from the client's personal directory
    private static List<String> getFilesFromPersonalDirectory() {
        List<String> playlist = new ArrayList<>();
        try {
            // Check if the personal directory exists
            if (Files.exists(personalDirectory)) {
                // List all .mp3 files in the directory and add them to the playlist
                Files.list(personalDirectory)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".mp3"))
                        .forEach(path -> playlist.add(path.getFileName().toString()));
            }
        } catch (Exception e) {
            // Print any exceptions that occur while reading the directory
            System.err.println("Error reading personal directory: " + e.getMessage());
        }
        return playlist;
    }

    // Method to refresh the list of connected peers from the Library service
    public static void refreshPeers() {
        try {
            // Retrieve the list of connected peers and update the connectedPeers list
            List<String> peers = musicLibrary.getConnectedPeers();
            connectedPeers.setAll(peers);
            System.out.println("Connected peers refreshed: " + peers);
        } catch (Exception e) {
            // Print any exceptions that occur during the refresh operation
            System.err.println("Error refreshing peers: " + e.getMessage());
        }
    }

    // Method to download .mp3 files from a peer's directory to a local downloads directory
    public static void downloadFromPeer(String peerAddress, String peerFolderPath, Path localDownloadsDirectory) {
        try {
            Path peerDirectory = Path.of(peerFolderPath);

            // Check if the peer directory exists and is a valid directory
            if (Files.exists(peerDirectory) && Files.isDirectory(peerDirectory)) {
                // List and filter .mp3 files from the peer's directory
                Files.list(peerDirectory)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".mp3"))
                        .forEach(path -> {
                            try {
                                // Copy each .mp3 file to the local downloads directory
                                Path targetFile = localDownloadsDirectory.resolve(path.getFileName());
                                Files.copy(path, targetFile);

                                System.out.println("File downloaded from peer: " + path.getFileName());
                            } catch (Exception e) {
                                // Print any exceptions that occur during file download
                                System.err.println("Error downloading file from peer: " + e.getMessage());
                            }
                        });
            } else {
                // Print an error if the peer directory does not exist or is invalid
                System.err.println("Peer directory does not exist or is invalid: " + peerDirectory);
            }
        } catch (Exception e) {
            // Print any exceptions that occur during the download process
            System.err.println("Error downloading from peer: " + e.getMessage());
        }
    }
}

package client;

import common.Library;
import common.PeerInfo;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;

public class Client extends Application {
    private static Library musicLibrary; // RMI interface to the server
    private static ObservableList<String> connectedPeers = FXCollections.observableArrayList(); // List for dynamic peer
                                                                                                // updates
    private static PeerInfoImpl peerInfo; // Client's peer information

    public static void main(String[] args) {
        try {
            // Connect to the RMI server
            musicLibrary = (Library) Naming.lookup("//localhost/Library");

            String peerAddress = "localhost"; 
            List<String> playlist = new ArrayList<>();
            playlist.add("song1.mp3");
            playlist.add("song2.mp3");
            peerInfo = new PeerInfoImpl(peerAddress, playlist);

            // Register this client with the server
            musicLibrary.registerPeer(peerInfo);

            // Launch the GUI
            launch(args);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Launch the GUI with dynamic peer updates
        MusicLibraryGUI gui = new MusicLibraryGUI(musicLibrary, connectedPeers);
        gui.start(primaryStage);

        // Start a background thread to refresh peers
        new Thread(this::refreshPeers).start();

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

    private void refreshPeers() {
        while (true) {
            try {
                // Fetch the list of connected peers from the server
                List<String> peers = musicLibrary.getConnectedPeers();

                // Update the observable list for the GUI
                connectedPeers.setAll(peers);

                // Sleep for 5 seconds before refreshing again
                Thread.sleep(5000);
            } catch (Exception e) {
                System.err.println("Error refreshing peers: " + e.getMessage());
            }
        }
    }
}

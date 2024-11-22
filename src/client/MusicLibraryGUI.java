package client;

import common.Library;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MusicLibraryGUI extends Application {
    private final Library musicLibrary; // RMI interface to the server
    private final ObservableList<String> connectedPeers; // Observable list for dynamic peer updates
    private final Handler handler = new Handler(); // Handler instance
    private final ObservableList<String> songObservableList = FXCollections.observableArrayList(); // For Browse & Play
    private final ObservableList<String> downloadedSongs = FXCollections.observableArrayList(); // For My Downloads
    private final ObservableList<String> communityPlaylist = FXCollections.observableArrayList(); // For Community
                                                                                                  // Playlist
    private Path currentClientDownloadsDirectory; // Unique download directory for this client
    private Path communityPlaylistDirectory; // Shared community playlist directory

    public MusicLibraryGUI(Library musicLibrary, ObservableList<String> connectedPeers,
            Path currentClientDownloadsDirectory) {
        this.musicLibrary = musicLibrary;
        this.connectedPeers = connectedPeers;
        this.currentClientDownloadsDirectory = currentClientDownloadsDirectory;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Music Library");

        // Initialize directories
        if (!initializeClientDirectories()) {
            handler.showError("Initialization Error", "Failed to initialize client directories.");
            return;
        }

        // Load songs into the observable list
        handler.loadSongs(songObservableList);

        // Tab Pane for navigation
        TabPane tabPane = new TabPane();

        // Add tabs to the pane
        tabPane.getTabs().add(createBrowsePlayTab());
        tabPane.getTabs().add(createMyDownloadsTab());
        tabPane.getTabs().add(createCommunityPlaylistTab());
        tabPane.getTabs().add(createMyPeersTab());

        // Set up the main scene
        Scene scene = new Scene(tabPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean initializeClientDirectories() {
        try {
            // Use the directory passed from the Client class
            if (currentClientDownloadsDirectory == null || !Files.exists(currentClientDownloadsDirectory)) {
                throw new IllegalStateException("Client downloads directory is not initialized or does not exist.");
            }

            // Set up the shared community playlist directory
            communityPlaylistDirectory = Path.of("../server/Playlist");
            Files.createDirectories(communityPlaylistDirectory);

            // Load community playlist into the observable list
            handler.loadCommunityPlaylist(communityPlaylistDirectory, communityPlaylist);
            return true;
        } catch (Exception e) {
            System.err.println("Error initializing directories: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Tab createBrowsePlayTab() {
        VBox playbackLayout = new VBox(10);
        playbackLayout.setAlignment(Pos.CENTER);
        playbackLayout.setPadding(new Insets(10));

        // Filtered list for searching songs
        FilteredList<String> filteredSongs = new FilteredList<>(songObservableList, p -> true);
        ListView<String> songList = new ListView<>(filteredSongs);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Songs...");
        searchField.textProperty().addListener((obs, oldText, newText) -> filteredSongs
                .setPredicate(song -> song.toLowerCase().contains(newText.toLowerCase())));

        Button playButton = new Button("▶ Play");
        Button pauseButton = new Button("⏸ Pause");
        Button stopButton = new Button("⏹ Stop");
        Button downloadButton = new Button("Download");

        playButton.setOnAction(e -> handler.playSelectedSong(songList));
        pauseButton.setOnAction(e -> handler.getPlayer().pause());
        stopButton.setOnAction(e -> handler.getPlayer().stop());
        downloadButton.setOnAction(
                e -> handler.downloadSelectedSong(songList, currentClientDownloadsDirectory, downloadedSongs));

        HBox controls = new HBox(10, playButton, pauseButton, stopButton, downloadButton);
        controls.setAlignment(Pos.CENTER);

        playbackLayout.getChildren().addAll(searchField, songList, controls);

        Tab browseTab = new Tab("Browse & Play", playbackLayout);
        browseTab.setClosable(false);
        return browseTab;
    }

    private Tab createMyDownloadsTab() {
        VBox downloadsLayout = new VBox(10);
        downloadsLayout.setAlignment(Pos.CENTER);
        downloadsLayout.setPadding(new Insets(10));

        // Filtered list for searching downloads
        FilteredList<String> filteredDownloads = new FilteredList<>(downloadedSongs, p -> true);
        ListView<String> downloadsList = new ListView<>(filteredDownloads);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Downloads...");
        searchField.textProperty().addListener((obs, oldText, newText) -> filteredDownloads
                .setPredicate(song -> song.toLowerCase().contains(newText.toLowerCase())));

        Button playDownloadedButton = new Button("▶ Play");
        Button pauseDownloadedButton = new Button("⏸ Pause");
        Button stopDownloadedButton = new Button("⏹ Stop");
        Button addToPlaylistButton = new Button("+ Add to Playlist");

        playDownloadedButton
                .setOnAction(e -> handler.playSelectedDownloadedSong(downloadsList, currentClientDownloadsDirectory));
        pauseDownloadedButton.setOnAction(e -> handler.getPlayer().pause());
        stopDownloadedButton.setOnAction(e -> handler.getPlayer().stop());
        addToPlaylistButton.setOnAction(e -> handler.addToCommunityPlaylist(
                downloadsList.getSelectionModel().getSelectedItem(),
                currentClientDownloadsDirectory,
                communityPlaylistDirectory,
                communityPlaylist));

        HBox downloadsControls = new HBox(10, playDownloadedButton, pauseDownloadedButton, stopDownloadedButton,
                addToPlaylistButton);
        downloadsControls.setAlignment(Pos.CENTER);

        downloadsLayout.getChildren().addAll(searchField, downloadsList, downloadsControls);

        Tab downloadsTab = new Tab("My Downloads", downloadsLayout);
        downloadsTab.setClosable(false);
        return downloadsTab;
    }

    private Tab createCommunityPlaylistTab() {
        VBox communityLayout = new VBox(10);
        communityLayout.setAlignment(Pos.CENTER);
        communityLayout.setPadding(new Insets(10));

        // Filtered list for searching the community playlist
        FilteredList<String> filteredCommunity = new FilteredList<>(communityPlaylist, p -> true);
        ListView<String> communityListView = new ListView<>(filteredCommunity);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Community Playlist...");
        searchField.textProperty().addListener((obs, oldText, newText) -> filteredCommunity
                .setPredicate(song -> song.toLowerCase().contains(newText.toLowerCase())));

        Button downloadFromPlaylistButton = new Button("Download from Playlist");
        Button removeFromPlaylistButton = new Button("Remove from Playlist");

        downloadFromPlaylistButton.setOnAction(e -> handler.downloadFromCommunityPlaylist(
                communityListView.getSelectionModel().getSelectedItem(),
                communityPlaylistDirectory,
                currentClientDownloadsDirectory,
                downloadedSongs));

        removeFromPlaylistButton.setOnAction(e -> handler.removeFromCommunityPlaylist(
                communityListView.getSelectionModel().getSelectedItem(),
                communityPlaylistDirectory,
                communityPlaylist));

        HBox communityControls = new HBox(10, downloadFromPlaylistButton, removeFromPlaylistButton);
        communityControls.setAlignment(Pos.CENTER);

        communityLayout.getChildren().addAll(searchField, communityListView, communityControls);

        Tab communityTab = new Tab("Community Playlist", communityLayout);
        communityTab.setClosable(false);
        return communityTab;
    }

    private Tab createMyPeersTab() {
        VBox peersLayout = new VBox(10);
        peersLayout.setAlignment(Pos.CENTER);
        peersLayout.setPadding(new Insets(10));

        ListView<String> peersListView = new ListView<>(connectedPeers);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Peers...");
        searchField.textProperty().addListener((obs, oldText, newText) -> connectedPeers
                .filtered(peer -> peer.toLowerCase().contains(newText.toLowerCase())));

        Button refreshPeersButton = new Button("Refresh Peers");
        Button downloadFromPeerButton = new Button("Download Songs from Peer");

        refreshPeersButton.setOnAction(e -> {
            try {
                List<String> peers = musicLibrary.getConnectedPeers();
                connectedPeers.setAll(peers);
            } catch (Exception ex) {
                showError("Error Refreshing Peers", "Could not refresh the peers list.");
            }
        });

        downloadFromPeerButton.setOnAction(e -> {
            String selectedPeer = peersListView.getSelectionModel().getSelectedItem();
            if (selectedPeer != null) {
                System.out.println("Selected peer: " + selectedPeer); // Debug log
                handler.downloadFromPeer(selectedPeer, currentClientDownloadsDirectory, downloadedSongs);
            } else {
                showError("No Peer Selected", "Please select a peer to download from.");
            }
        });

        HBox peersControls = new HBox(10, refreshPeersButton, downloadFromPeerButton);
        peersControls.setAlignment(Pos.CENTER);

        peersLayout.getChildren().addAll(searchField, peersListView, peersControls);

        Tab peersTab = new Tab("My Peers", peersLayout);
        peersTab.setClosable(false);
        return peersTab;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

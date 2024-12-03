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
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MusicLibraryGUI extends Application {
    // The main library object to hold the music collection
    private final Library musicLibrary;
    // List of peers connected to the music network
    private final ObservableList<String> connectedPeers;
    // Handler for managing media playback and other actions
    private final Handler handler = new Handler();
    // Observable list to manage the songs displayed in the browse/play tab
    private final ObservableList<String> songObservableList = FXCollections.observableArrayList();
    // Observable list for songs that have been downloaded by the client
    private final ObservableList<String> downloadedSongs = FXCollections.observableArrayList();
    // Observable list for the community playlist
    private final ObservableList<String> communityPlaylist = FXCollections.observableArrayList();
    // Path to the current client's downloads directory
    private Path currentClientDownloadsDirectory;
    // Path to the community playlist directory on the server
    private Path communityPlaylistDirectory;

    // Constructor to initialize the GUI with library data and client paths
    public MusicLibraryGUI(Library musicLibrary, ObservableList<String> connectedPeers,
            Path currentClientDownloadsDirectory) {
        this.musicLibrary = musicLibrary;
        this.connectedPeers = connectedPeers;
        this.currentClientDownloadsDirectory = currentClientDownloadsDirectory;
    }

    @Override
    public void start(Stage primaryStage) {
        // Set the title of the primary stage
        primaryStage.setTitle("Music Library");

        // Initialize the client directories and handle errors if any
        if (!initializeClientDirectories()) {
            handler.showError("Initialization Error", "Failed to initialize client directories.");
            return;
        }

        // Load the list of songs into the song observable list
        handler.loadSongs(songObservableList);

        // Create individual tabs for browsing and playing, downloads, community playlist, and peers
        Tab browsePlayTab = createBrowsePlayTab();
        Tab myDownloadsTab = createMyDownloadsTab();
        Tab communityPlaylistTab = createCommunityPlaylistTab();
        Tab myPeersTab = createMyPeersTab();

        // Create a TabPane to hold the different tabs and set its style
        TabPane tabPane = new TabPane(browsePlayTab, myDownloadsTab, communityPlaylistTab, myPeersTab);
        tabPane.setTabMinWidth(0);
        tabPane.setTabMaxWidth(0);
        tabPane.setTabMinHeight(0);
        tabPane.setTabMaxHeight(0);
        tabPane.setStyle("-fx-background-color: #3d3d3d;");

        // Create buttons to switch between tabs and set their styles
        Button browsePlayButton = new Button("Browse & Play");
        Button myDownloadsButton = new Button("My Downloads");
        Button communityPlaylistButton = new Button("Community Playlist");
        Button myPeersButton = new Button("My Peers");

        // Styling for the buttons
        String buttonStyle = "-fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: #2b2b2b; -fx-padding: 10;";
        String buttonHoverStyle = "-fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: #404040; -fx-padding: 10;";
        browsePlayButton.setStyle(buttonStyle);
        myDownloadsButton.setStyle(buttonStyle);
        communityPlaylistButton.setStyle(buttonStyle);
        myPeersButton.setStyle(buttonStyle);

        // Add hover effects to the buttons
        addHoverEffect(browsePlayButton, buttonStyle, buttonHoverStyle);
        addHoverEffect(myDownloadsButton, buttonStyle, buttonHoverStyle);
        addHoverEffect(communityPlaylistButton, buttonStyle, buttonHoverStyle);
        addHoverEffect(myPeersButton, buttonStyle, buttonHoverStyle);

        // Set buttons to expand to the maximum width available
        browsePlayButton.setMaxWidth(Double.MAX_VALUE);
        myDownloadsButton.setMaxWidth(Double.MAX_VALUE);
        communityPlaylistButton.setMaxWidth(Double.MAX_VALUE);
        myPeersButton.setMaxWidth(Double.MAX_VALUE);

        // Set the actions for each button to switch tabs when clicked
        browsePlayButton.setOnAction(e -> tabPane.getSelectionModel().select(browsePlayTab));
        myDownloadsButton.setOnAction(e -> tabPane.getSelectionModel().select(myDownloadsTab));
        communityPlaylistButton.setOnAction(e -> tabPane.getSelectionModel().select(communityPlaylistTab));
        myPeersButton.setOnAction(e -> tabPane.getSelectionModel().select(myPeersTab));

        // Create a VBox to hold the tab buttons and set its properties
        VBox tabButtonBox = new VBox(10, browsePlayButton, myDownloadsButton, communityPlaylistButton, myPeersButton);
        tabButtonBox.setPadding(new Insets(0));
        tabButtonBox.setSpacing(10);
        tabButtonBox.setStyle("-fx-background-color: #2b2b2b;");
        tabButtonBox.setAlignment(Pos.TOP_CENTER);

        // Set the vertical grow policy for the buttons
        VBox.setVgrow(browsePlayButton, Priority.ALWAYS);
        VBox.setVgrow(myDownloadsButton, Priority.ALWAYS);
        VBox.setVgrow(communityPlaylistButton, Priority.ALWAYS);
        VBox.setVgrow(myPeersButton, Priority.ALWAYS);

        // Create the main layout and set the scene
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(tabButtonBox);
        mainLayout.setCenter(tabPane);
        BorderPane.setMargin(tabPane, Insets.EMPTY);

        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to add hover effects to buttons
    private void addHoverEffect(Button button, String normalStyle, String hoverStyle) {
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }

    // Method to initialize client directories and load community playlist
    private boolean initializeClientDirectories() {
        try {
            // Check if the client downloads directory is valid
            if (currentClientDownloadsDirectory == null || !Files.exists(currentClientDownloadsDirectory)) {
                throw new IllegalStateException("Client downloads directory is not initialized or does not exist.");
            }

            // Create or verify the community playlist directory
            communityPlaylistDirectory = Path.of("../server/Playlist");
            Files.createDirectories(communityPlaylistDirectory);

            // Load the community playlist
            handler.loadCommunityPlaylist(communityPlaylistDirectory, communityPlaylist);
            return true;
        } catch (Exception e) {
            // Print error details and stack trace if directory initialization fails
            System.err.println("Error initializing directories: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Method to create the 'Browse & Play' tab
    private Tab createBrowsePlayTab() {
        VBox playbackLayout = new VBox(10);
        playbackLayout.setAlignment(Pos.CENTER);
        playbackLayout.setPadding(new Insets(10));

        // Title for the 'Browse & Play' tab
        Label title = new Label("Browse and Listen");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        // Filtered list to enable song searching functionality
        FilteredList<String> filteredSongs = new FilteredList<>(songObservableList, p -> true);
        ListView<String> songList = new ListView<>(filteredSongs);

        // Search bar to filter songs in the list
        TextField searchField = new TextField();
        searchField.setPromptText("Search Songs...");
        searchField.textProperty().addListener((obs, oldText, newText) -> filteredSongs
                .setPredicate(song -> song.toLowerCase().contains(newText.toLowerCase())));

        // Progress slider for playback control
        Slider progressSlider = new Slider();
        progressSlider.setMin(0);
        progressSlider.setMax(1);
        progressSlider.setValue(0);
        progressSlider.setDisable(true);
        progressSlider.setStyle(
                "-fx-control-inner-background: green; -fx-thumb-background: #d94e7b;");

        // Label to display current and total playback time
        Label timeLabel = new Label("0:00 / 0:00");
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");

        // Buttons for controlling playback
        Button playButton = new Button("▶ Play");
        Button pauseButton = new Button("⏸ Pause");
        Button stopButton = new Button("⏹ Stop");
        Button downloadButton = new Button("Download");

        // Style for buttons
        String buttonStyle = "-fx-background-color: #d94e7b; -fx-text-fill: white; -fx-font-size: 14px;";
        playButton.setStyle(buttonStyle);
        pauseButton.setStyle(buttonStyle);
        stopButton.setStyle(buttonStyle);
        downloadButton.setStyle(buttonStyle);

        // Action for the 'Play' button to start playback
        playButton.setOnAction(e -> {
            handler.playSelectedSong(songList);

            if (handler.getPlayer() != null) {
                progressSlider.setDisable(false);
                progressSlider.setValue(0);

                handler.getPlayer().getMediaPlayer().setOnReady(() -> {
                    System.out.println("MediaPlayer is ready. Starting playback...");

                    progressSlider.setMax(handler.getPlayer().getMediaPlayer().getTotalDuration().toSeconds());
                    handler.getPlayer().play();
                });
            }
        });

        // Action for the 'Pause' button
        pauseButton.setOnAction(e -> handler.getPlayer().pause());

        // Action for the 'Stop' button
        stopButton.setOnAction(e -> handler.getPlayer().stop());

        // Action for the 'Download' button
        downloadButton.setOnAction(e -> handler.downloadSelectedSong(songList, currentClientDownloadsDirectory));

        // Add components to the layout
        playbackLayout.getChildren().addAll(title, searchField, songList, playButton, pauseButton, stopButton, downloadButton, timeLabel, progressSlider);

        // Create and return the tab
        Tab browsePlayTab = new Tab("Browse & Play");
        browsePlayTab.setContent(playbackLayout);
        browsePlayTab.setClosable(false);
        return browsePlayTab;
    }

    // Method to create the 'My Downloads' tab
    private Tab createMyDownloadsTab() {
        // Implement layout and functionality for 'My Downloads' tab here
        return new Tab("My Downloads");
    }

    // Method to create the 'Community Playlist' tab
    private Tab createCommunityPlaylistTab() {
        // Implement layout and functionality for 'Community Playlist' tab here
        return new Tab("Community Playlist");
    }

    // Method to create the 'My Peers' tab
    private Tab createMyPeersTab() {
        // Implement layout and functionality for 'My Peers' tab here
        return new Tab("My Peers");
    }
}

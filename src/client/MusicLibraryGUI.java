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
    private final Library musicLibrary;
    private final ObservableList<String> connectedPeers;
    private final Handler handler = new Handler();
    private final ObservableList<String> songObservableList = FXCollections.observableArrayList();
    private final ObservableList<String> downloadedSongs = FXCollections.observableArrayList();
    private final ObservableList<String> communityPlaylist = FXCollections.observableArrayList();
    private Path currentClientDownloadsDirectory;
    private Path communityPlaylistDirectory;

    public MusicLibraryGUI(Library musicLibrary, ObservableList<String> connectedPeers,
            Path currentClientDownloadsDirectory) {
        this.musicLibrary = musicLibrary;
        this.connectedPeers = connectedPeers;
        this.currentClientDownloadsDirectory = currentClientDownloadsDirectory;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Music Library");

        if (!initializeClientDirectories()) {
            handler.showError("Initialization Error", "Failed to initialize client directories.");
            return;
        }

        handler.loadSongs(songObservableList);

        Tab browsePlayTab = createBrowsePlayTab();
        Tab myDownloadsTab = createMyDownloadsTab();
        Tab communityPlaylistTab = createCommunityPlaylistTab();
        Tab myPeersTab = createMyPeersTab();

        TabPane tabPane = new TabPane(browsePlayTab, myDownloadsTab, communityPlaylistTab, myPeersTab);
        tabPane.setTabMinWidth(0);
        tabPane.setTabMaxWidth(0);
        tabPane.setTabMinHeight(0);
        tabPane.setTabMaxHeight(0);
        tabPane.setStyle("-fx-background-color: #3d3d3d;");

        Button browsePlayButton = new Button("Browse & Play");
        Button myDownloadsButton = new Button("My Downloads");
        Button communityPlaylistButton = new Button("Community Playlist");
        Button myPeersButton = new Button("My Peers");

        String buttonStyle = "-fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: #2b2b2b; -fx-padding: 10;";
        String buttonHoverStyle = "-fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: #404040; -fx-padding: 10;";
        browsePlayButton.setStyle(buttonStyle);
        myDownloadsButton.setStyle(buttonStyle);
        communityPlaylistButton.setStyle(buttonStyle);
        myPeersButton.setStyle(buttonStyle);

        addHoverEffect(browsePlayButton, buttonStyle, buttonHoverStyle);
        addHoverEffect(myDownloadsButton, buttonStyle, buttonHoverStyle);
        addHoverEffect(communityPlaylistButton, buttonStyle, buttonHoverStyle);
        addHoverEffect(myPeersButton, buttonStyle, buttonHoverStyle);

        browsePlayButton.setMaxWidth(Double.MAX_VALUE);
        myDownloadsButton.setMaxWidth(Double.MAX_VALUE);
        communityPlaylistButton.setMaxWidth(Double.MAX_VALUE);
        myPeersButton.setMaxWidth(Double.MAX_VALUE);

        browsePlayButton.setOnAction(e -> tabPane.getSelectionModel().select(browsePlayTab));
        myDownloadsButton.setOnAction(e -> tabPane.getSelectionModel().select(myDownloadsTab));
        communityPlaylistButton.setOnAction(e -> tabPane.getSelectionModel().select(communityPlaylistTab));
        myPeersButton.setOnAction(e -> tabPane.getSelectionModel().select(myPeersTab));

        VBox tabButtonBox = new VBox(10, browsePlayButton, myDownloadsButton, communityPlaylistButton, myPeersButton);
        tabButtonBox.setPadding(new Insets(0));
        tabButtonBox.setSpacing(10);
        tabButtonBox.setStyle("-fx-background-color: #2b2b2b;");
        tabButtonBox.setAlignment(Pos.TOP_CENTER);

        VBox.setVgrow(browsePlayButton, Priority.ALWAYS);
        VBox.setVgrow(myDownloadsButton, Priority.ALWAYS);
        VBox.setVgrow(communityPlaylistButton, Priority.ALWAYS);
        VBox.setVgrow(myPeersButton, Priority.ALWAYS);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(tabButtonBox);
        mainLayout.setCenter(tabPane);
        BorderPane.setMargin(tabPane, Insets.EMPTY);

        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addHoverEffect(Button button, String normalStyle, String hoverStyle) {
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }

    private boolean initializeClientDirectories() {
        try {

            if (currentClientDownloadsDirectory == null || !Files.exists(currentClientDownloadsDirectory)) {
                throw new IllegalStateException("Client downloads directory is not initialized or does not exist.");
            }

            communityPlaylistDirectory = Path.of("../server/Playlist");
            Files.createDirectories(communityPlaylistDirectory);

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

        Label title = new Label("Browse and Listen");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        FilteredList<String> filteredSongs = new FilteredList<>(songObservableList, p -> true);
        ListView<String> songList = new ListView<>(filteredSongs);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Songs...");
        searchField.textProperty().addListener((obs, oldText, newText) -> filteredSongs
                .setPredicate(song -> song.toLowerCase().contains(newText.toLowerCase())));

        Slider progressSlider = new Slider();
        progressSlider.setMin(0);
        progressSlider.setMax(1);
        progressSlider.setValue(0);
        progressSlider.setDisable(true);
        progressSlider.setStyle(
                "-fx-control-inner-background: green; -fx-thumb-background: #d94e7b;");

        Label timeLabel = new Label("0:00 / 0:00");
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");

        Button playButton = new Button("▶ Play");
        Button pauseButton = new Button("⏸ Pause");
        Button stopButton = new Button("⏹ Stop");
        Button downloadButton = new Button("Download");

        String buttonStyle = "-fx-background-color: #d94e7b; -fx-text-fill: white; -fx-font-size: 14px;";
        playButton.setStyle(buttonStyle);
        pauseButton.setStyle(buttonStyle);
        stopButton.setStyle(buttonStyle);
        downloadButton.setStyle(buttonStyle);

        playButton.setOnAction(e -> {
            handler.playSelectedSong(songList);

            if (handler.getPlayer() != null) {
                progressSlider.setDisable(false);
                progressSlider.setValue(0);

                handler.getPlayer().getMediaPlayer().setOnReady(() -> {
                    System.out.println("MediaPlayer is ready. Starting playback...");

                    progressSlider.setMax(handler.getPlayer().getTotalDuration().toSeconds());
                    timeLabel.setText("0:00 / " + formatTime(handler.getPlayer().getTotalDuration()));

                    handler.getPlayer().currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                        if (!progressSlider.isValueChanging()) {
                            progressSlider.setValue(newTime.toSeconds());
                        }

                        timeLabel.setText(
                                formatTime(newTime) + " / " + formatTime(handler.getPlayer().getTotalDuration()));
                    });

                    handler.getPlayer().play();
                });
            }
        });

        pauseButton.setOnAction(e -> {
            if (handler.getPlayer() != null) {
                handler.getPlayer().pause();
            }
        });

        stopButton.setOnAction(e -> {
            if (handler.getPlayer() != null) {
                handler.getPlayer().stop();
            }
            progressSlider.setValue(0);
            progressSlider.setDisable(true);
            timeLabel.setText("0:00 / 0:00");
        });

        downloadButton.setOnAction(e -> {
            handler.downloadSelectedSong(songList, currentClientDownloadsDirectory, downloadedSongs);
        });

        progressSlider.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
            if (!isNowChanging) {
                if (handler.getPlayer() != null && handler.getPlayer().getMediaPlayer() != null) {
                    Duration seekTime = Duration.seconds(progressSlider.getValue());
                    handler.getPlayer().seek(seekTime);
                    System.out.println("Seeking to: " + formatTime(seekTime));
                }
            }
        });

        progressSlider.setOnMousePressed(event -> {
            if (handler.getPlayer() != null && handler.getPlayer().getMediaPlayer() != null) {
                double mouseX = event.getX();
                double sliderWidth = progressSlider.getWidth();
                double percent = mouseX / sliderWidth;
                double seekTimeInSeconds = percent * progressSlider.getMax();

                progressSlider.setValue(seekTimeInSeconds);
                Duration seekTime = Duration.seconds(seekTimeInSeconds);
                handler.getPlayer().seek(seekTime);
                System.out.println("Tapped slider, seeking to: " + formatTime(seekTime));
            }
        });

        HBox controls = new HBox(10, playButton, pauseButton, stopButton, downloadButton);
        controls.setAlignment(Pos.CENTER);

        playbackLayout.getChildren().addAll(title, searchField, songList, progressSlider, timeLabel, controls);

        Tab browseTab = new Tab("Browse & Play", playbackLayout);
        browseTab.setClosable(false);
        return browseTab;
    }

    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "0:00";
        }
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    private Tab createMyDownloadsTab() {
        VBox downloadsLayout = new VBox(10);
        downloadsLayout.setAlignment(Pos.CENTER);
        downloadsLayout.setPadding(new Insets(10));

        Label title = new Label("My Downloads");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        FilteredList<String> filteredDownloads = new FilteredList<>(downloadedSongs, p -> true);
        ListView<String> downloadsList = new ListView<>(filteredDownloads);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Downloads...");
        searchField.textProperty().addListener((obs, oldText, newText) -> filteredDownloads
                .setPredicate(song -> song.toLowerCase().contains(newText.toLowerCase())));

        Slider progressSlider = new Slider();
        progressSlider.setMin(0);
        progressSlider.setMax(1);
        progressSlider.setValue(0);
        progressSlider.setDisable(true);
        progressSlider.setStyle(
                "-fx-control-inner-background: green; -fx-thumb-background: #d94e7b;");

        Label timeLabel = new Label("0:00 / 0:00");
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");

        Button playDownloadedButton = new Button("▶ Play");
        Button pauseDownloadedButton = new Button("⏸ Pause");
        Button stopDownloadedButton = new Button("⏹ Stop");
        Button addToPlaylistButton = new Button("+ Add to Playlist");

        String buttonStyle = "-fx-background-color: #d94e7b; -fx-text-fill: white; -fx-font-size: 14px;";
        playDownloadedButton.setStyle(buttonStyle);
        pauseDownloadedButton.setStyle(buttonStyle);
        stopDownloadedButton.setStyle(buttonStyle);
        addToPlaylistButton.setStyle(buttonStyle);

        playDownloadedButton.setOnAction(e -> {
            handler.playSelectedDownloadedSong(downloadsList, currentClientDownloadsDirectory);

            if (handler.getPlayer() != null) {
                progressSlider.setDisable(false);
                progressSlider.setValue(0);

                handler.getPlayer().getMediaPlayer().setOnReady(() -> {
                    System.out.println("MediaPlayer is ready. Starting playback...");

                    progressSlider.setMax(handler.getPlayer().getTotalDuration().toSeconds());
                    timeLabel.setText("0:00 / " + formatTime(handler.getPlayer().getTotalDuration()));

                    handler.getPlayer().currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                        if (!progressSlider.isValueChanging()) {
                            progressSlider.setValue(newTime.toSeconds());
                        }
                        timeLabel.setText(
                                formatTime(newTime) + " / " + formatTime(handler.getPlayer().getTotalDuration()));
                    });

                    handler.getPlayer().play();
                });
            }
        });

        pauseDownloadedButton.setOnAction(e -> {
            if (handler.getPlayer() != null) {
                handler.getPlayer().pause();
            }
        });

        stopDownloadedButton.setOnAction(e -> {
            if (handler.getPlayer() != null) {
                handler.getPlayer().stop();
            }
            progressSlider.setValue(0);
            progressSlider.setDisable(true);
            timeLabel.setText("0:00 / 0:00");
        });

        addToPlaylistButton.setOnAction(e -> {
            handler.addToCommunityPlaylist(
                    downloadsList.getSelectionModel().getSelectedItem(),
                    currentClientDownloadsDirectory,
                    communityPlaylistDirectory,
                    communityPlaylist);
        });

        progressSlider.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
            if (!isNowChanging) {
                if (handler.getPlayer() != null && handler.getPlayer().getMediaPlayer() != null) {
                    Duration seekTime = Duration.seconds(progressSlider.getValue());
                    handler.getPlayer().seek(seekTime);
                    System.out.println("Seeking to: " + formatTime(seekTime));
                }
            }
        });

        progressSlider.setOnMousePressed(event -> {
            if (handler.getPlayer() != null && handler.getPlayer().getMediaPlayer() != null) {
                double mouseX = event.getX();
                double sliderWidth = progressSlider.getWidth();
                double percent = mouseX / sliderWidth;
                double seekTimeInSeconds = percent * progressSlider.getMax();

                progressSlider.setValue(seekTimeInSeconds);
                Duration seekTime = Duration.seconds(seekTimeInSeconds);
                handler.getPlayer().seek(seekTime);
                System.out.println("Tapped slider, seeking to: " + formatTime(seekTime));
            }
        });

        HBox downloadsControls = new HBox(10, playDownloadedButton, pauseDownloadedButton, stopDownloadedButton,
                addToPlaylistButton);
        downloadsControls.setAlignment(Pos.CENTER);

        downloadsLayout.getChildren().addAll(title, searchField, downloadsList, progressSlider, timeLabel,
                downloadsControls);

        Tab downloadsTab = new Tab("My Downloads", downloadsLayout);
        downloadsTab.setClosable(false);
        return downloadsTab;
    }

    private Tab createCommunityPlaylistTab() {
        VBox communityLayout = new VBox(10);
        communityLayout.setAlignment(Pos.CENTER);
        communityLayout.setPadding(new Insets(10));

        Label title = new Label("Community Playlist");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        FilteredList<String> filteredCommunity = new FilteredList<>(communityPlaylist, p -> true);
        ListView<String> communityListView = new ListView<>(filteredCommunity);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Community Playlist...");
        searchField.textProperty().addListener((obs, oldText, newText) -> filteredCommunity
                .setPredicate(song -> song.toLowerCase().contains(newText.toLowerCase())));

        Button downloadFromPlaylistButton = new Button("Download from Playlist");
        Button removeFromPlaylistButton = new Button("Remove from Playlist");

        String buttonStyle = "-fx-background-color: #d94e7b; -fx-text-fill: white; -fx-font-size: 14px;";
        downloadFromPlaylistButton.setStyle(buttonStyle);
        removeFromPlaylistButton.setStyle(buttonStyle);

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

        communityLayout.getChildren().addAll(title, searchField, communityListView, communityControls);

        Tab communityTab = new Tab("Community Playlist", communityLayout);
        communityTab.setClosable(false);
        return communityTab;
    }

    private Tab createMyPeersTab() {
        VBox peersLayout = new VBox(10);
        peersLayout.setAlignment(Pos.CENTER);
        peersLayout.setPadding(new Insets(10));

        Label title = new Label("My Peers");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        ListView<String> peersListView = new ListView<>(connectedPeers);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Peers...");
        searchField.textProperty().addListener((obs, oldText, newText) -> connectedPeers
                .filtered(peer -> peer.toLowerCase().contains(newText.toLowerCase())));

        Button refreshPeersButton = new Button("Refresh Peers");
        Button downloadFromPeerButton = new Button("Download Songs from Peer");

        String buttonStyle = "-fx-background-color: #d94e7b; -fx-text-fill: white; -fx-font-size: 14px;";
        refreshPeersButton.setStyle(buttonStyle);
        downloadFromPeerButton.setStyle(buttonStyle);

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
                System.out.println("Selected peer: " + selectedPeer);
                handler.downloadFromPeer(selectedPeer, currentClientDownloadsDirectory, downloadedSongs);
            } else {
                showError("No Peer Selected", "Please select a peer to download from.");
            }
        });

        HBox peersControls = new HBox(10, refreshPeersButton, downloadFromPeerButton);
        peersControls.setAlignment(Pos.CENTER);

        peersLayout.getChildren().addAll(title, searchField, peersListView, peersControls);

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

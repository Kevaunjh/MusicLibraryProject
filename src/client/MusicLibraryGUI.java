import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MusicLibraryGUI extends Application {
    private final Handler handler = new Handler();
    private final ObservableList<String> downloadedSongs = FXCollections.observableArrayList();
    private final ObservableList<String> communityPlaylist = FXCollections.observableArrayList();
    private Path currentClientDownloadsDirectory;
    private Path communityPlaylistDirectory;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Music Library");

        // Initialize directories
        try {
            Path clientDirectory = Path.of("client");
            if (!Files.exists(clientDirectory)) {
                Files.createDirectory(clientDirectory);
            }
            currentClientDownloadsDirectory = Files.createTempDirectory(clientDirectory, "CurrentClientDownloads");

            communityPlaylistDirectory = Path.of("CommunityPlaylist");
            if (!Files.exists(communityPlaylistDirectory)) {
                Files.createDirectory(communityPlaylistDirectory);
            }
            handler.loadCommunityPlaylist(communityPlaylistDirectory, communityPlaylist);
        } catch (IOException e) {
            handler.showError("Initialization Error", "Failed to create necessary directories.");
            return;
        }

        // Browse & Play Tab
        VBox playbackLayout = new VBox(10);
        playbackLayout.setAlignment(Pos.CENTER);
        playbackLayout.setPadding(new Insets(10));

        ListView<String> songList = new ListView<>();
        handler.loadSongs(songList);

        Button playButton = new Button("▶ Play");
        Button pauseButton = new Button("⏸ Pause");
        Button stopButton = new Button("⏹ Stop");
        Button downloadButton = new Button("Download");
        Button addPlaylistButton = new Button("+ Add to Community Playlist");

        playButton.setOnAction(e -> handler.playSelectedSong(songList));
        pauseButton.setOnAction(e -> handler.getPlayer().pause());
        stopButton.setOnAction(e -> handler.getPlayer().stop());
        downloadButton.setOnAction(
                e -> handler.downloadSelectedSong(songList, currentClientDownloadsDirectory, downloadedSongs));
        addPlaylistButton.setOnAction(e -> handler.addToCommunityPlaylist(
                songList.getSelectionModel().getSelectedItem(),
                currentClientDownloadsDirectory,
                communityPlaylistDirectory,
                communityPlaylist));

        HBox controls = new HBox(10, playButton, pauseButton, stopButton, downloadButton, addPlaylistButton);
        controls.setAlignment(Pos.CENTER);

        playbackLayout.getChildren().addAll(songList, controls);

        // Community Playlist Tab
        VBox communityLayout = new VBox(10);
        communityLayout.setAlignment(Pos.CENTER);
        communityLayout.setPadding(new Insets(10));

        ListView<String> communityListView = new ListView<>(communityPlaylist);
        Label communityLabel = new Label("Community Playlist");

        Button downloadFromPlaylistButton = new Button("Download from Playlist");
        downloadFromPlaylistButton.setOnAction(e -> handler.downloadFromCommunityPlaylist(
                communityListView.getSelectionModel().getSelectedItem(),
                communityPlaylistDirectory,
                currentClientDownloadsDirectory,
                downloadedSongs));

        Button removeFromPlaylistButton = new Button("Remove from Playlist");
        removeFromPlaylistButton.setOnAction(e -> handler.removeFromCommunityPlaylist(
                communityListView.getSelectionModel().getSelectedItem(),
                communityPlaylistDirectory,
                communityPlaylist));

        HBox communityControls = new HBox(10, downloadFromPlaylistButton, removeFromPlaylistButton);
        communityControls.setAlignment(Pos.CENTER);

        communityLayout.getChildren().addAll(communityLabel, communityListView, communityControls);

        // My Downloads Tab
        VBox downloadsLayout = new VBox(10);
        downloadsLayout.setAlignment(Pos.CENTER);
        downloadsLayout.setPadding(new Insets(10));

        ListView<String> downloadsList = new ListView<>(downloadedSongs);
        Label downloadsLabel = new Label("My Downloads");

        Button playDownloadedButton = new Button("▶ Play");
        Button pauseDownloadedButton = new Button("⏸ Pause");
        Button stopDownloadedButton = new Button("⏹ Stop");

        playDownloadedButton
                .setOnAction(e -> handler.playSelectedDownloadedSong(downloadsList, currentClientDownloadsDirectory));
        pauseDownloadedButton.setOnAction(e -> handler.getPlayer().pause());
        stopDownloadedButton.setOnAction(e -> handler.getPlayer().stop());

        HBox downloadsControls = new HBox(10, playDownloadedButton, pauseDownloadedButton, stopDownloadedButton);
        downloadsControls.setAlignment(Pos.CENTER);

        downloadsLayout.getChildren().addAll(downloadsLabel, downloadsList, downloadsControls);

        // Tab Pane
        TabPane tabPane = new TabPane();
        Tab browseTab = new Tab("Browse & Play", playbackLayout);
        browseTab.setClosable(false);
        Tab communityTab = new Tab("Community Playlist", communityLayout);
        communityTab.setClosable(false);
        Tab downloadsTab = new Tab("My Downloads", downloadsLayout);
        downloadsTab.setClosable(false);

        tabPane.getTabs().addAll(browseTab, communityTab, downloadsTab);

        Scene scene = new Scene(tabPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

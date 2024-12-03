import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Implementation of the Library interface for managing a music library.
 * Extends UnicastRemoteObject to support remote method invocation (RMI).
 */
public class LibraryImpl extends UnicastRemoteObject implements Library {
    private Map<String, File> songs = new HashMap<>(); // Map to store song names and their corresponding File objects.
    private Map<String, List<Integer>> ratings = new HashMap<>(); // Map to store ratings for each song.
    private Map<String, String> metadata = new HashMap<>(); // Map to store metadata information for each song.

    // Constructor that initializes the LibraryImpl instance and loads existing songs from the "Songs" directory.
    public LibraryImpl() throws RemoteException {
        super();
        loadSongs(); // Load songs from the "Songs" directory during initialization.
    }

    /**
     * Loads songs from the "Songs" directory into the songs map.
     */
    private void loadSongs() {
        File songDir = new File("Songs");
        if (!songDir.exists()) {
            songDir.mkdir(); // Create the "Songs" directory if it doesn't exist.
            System.out.println("Songs directory created.");
        }

        // Iterate through files in the directory and add them to the songs map if they are .mp3 files.
        for (File song : songDir.listFiles()) {
            if (song.isFile() && song.getName().endsWith(".mp3")) {
                songs.put(song.getName(), song);
                metadata.put(song.getName(), "Artist: Unknown, Album: Unknown, Duration: Unknown");
                System.out.println("Loaded song: " + song.getName());
            }
        }
    }

    @Override
    public List<String> searchSongs(String query) throws RemoteException {
        List<String> results = new ArrayList<>();
        for (String song : songs.keySet()) {
            if (song.toLowerCase().contains(query.toLowerCase())) {
                results.add(song);
            }
        }
        return results;
    }

    @Override
    public byte[] streamSong(String songName) throws RemoteException {
        try {
            File song = songs.get(songName);
            if (song == null)
                throw new FileNotFoundException("Song not found!");

            byte[] data = new byte[(int) song.length()];
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(song))) {
                bis.read(data);
            }
            return data;
        } catch (IOException e) {
            throw new RemoteException("Error streaming song: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean uploadSong(String songName, byte[] data) throws RemoteException {
        try {
            File song = new File("Songs/" + songName);
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(song))) {
                bos.write(data);
            }
            songs.put(songName, song);
            metadata.put(songName, "Uploaded by user, Artist: Unknown, Album: Unknown, Duration: Unknown");
            ratings.put(songName, new ArrayList<>());
            return true;
        } catch (IOException e) {
            throw new RemoteException("Error uploading song: " + e.getMessage(), e);
        }
    }

    @Override
    public String getSongMetadata(String songName) throws RemoteException {
        return metadata.getOrDefault(songName, "Metadata not found!");
    }

    @Override
    public boolean rateSong(String songName, int rating) throws RemoteException {
        if (!songs.containsKey(songName)) {
            throw new RemoteException("Song not found!");
        }
        ratings.putIfAbsent(songName, new ArrayList<>());
        ratings.get(songName).add(rating);

        // Calculate the average rating and update the metadata.
        double average = ratings.get(songName).stream().mapToInt(Integer::intValue).average().orElse(0);
        metadata.put(songName, metadata.get(songName) + ", Average Rating: " + String.format("%.2f", average));
        return true;
    }
}

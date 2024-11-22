package server;

import common.Library;
import common.PeerInfo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RMIServerImpl extends UnicastRemoteObject implements Library {
    private final List<PeerInfo> connectedPeers;
    private final Map<String, String> songMetadata;
    private final Map<String, Integer> songRatings;

    public RMIServerImpl() throws RemoteException {
        super();
        connectedPeers = new ArrayList<>();
        songMetadata = new HashMap<>();
        songRatings = new HashMap<>();

        // Example data
        songMetadata.put("song1.mp3", "Artist: Artist1, Album: Album1");
        songMetadata.put("song2.mp3", "Artist: Artist2, Album: Album2");
        songRatings.put("song1.mp3", 5);
        songRatings.put("song2.mp3", 4);
    }

    @Override
    public synchronized void registerPeer(PeerInfo peer) throws RemoteException {
        connectedPeers.add(peer);
        System.out.println("Peer connected: " + peer.getPeerAddress());
    }

    @Override
    public synchronized void deregisterPeer(PeerInfo peer) throws RemoteException {
        connectedPeers.remove(peer);
        System.out.println("Peer disconnected: " + peer.getPeerAddress());
    }

    @Override
    public synchronized List<String> getConnectedPeers() throws RemoteException {
        List<String> peerAddresses = new ArrayList<>();
        for (PeerInfo peer : connectedPeers) {
            peerAddresses.add(peer.getPeerAddress());
        }
        return peerAddresses;
    }

    // Implement the new methods
    @Override
    public List<String> searchSongs(String query) throws RemoteException {
        List<String> results = new ArrayList<>();
        for (String song : songMetadata.keySet()) {
            if (song.toLowerCase().contains(query.toLowerCase())) {
                results.add(song);
            }
        }
        return results;
    }

    @Override
    public byte[] streamSong(String songName) throws RemoteException {
        // Simulate streaming with a placeholder response
        System.out.println("Streaming song: " + songName);
        return songName.getBytes();
    }

    @Override
    public String getSongMetadata(String songName) throws RemoteException {
        return songMetadata.getOrDefault(songName, "Metadata not available");
    }

    @Override
    public void rateSong(String songName, int rating) throws RemoteException {
        songRatings.put(songName, rating);
        System.out.println("Song rated: " + songName + " with rating " + rating);
    }
}

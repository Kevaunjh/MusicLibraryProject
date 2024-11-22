package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Library extends Remote {
    List<String> getConnectedPeers() throws RemoteException;

    void registerPeer(PeerInfo peer) throws RemoteException;

    void deregisterPeer(PeerInfo peer) throws RemoteException;

    // Add these methods
    List<String> searchSongs(String query) throws RemoteException;

    byte[] streamSong(String songName) throws RemoteException;

    String getSongMetadata(String songName) throws RemoteException;

    void rateSong(String songName, int rating) throws RemoteException;
}

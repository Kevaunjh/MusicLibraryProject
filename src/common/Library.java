package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * The Library interface defines the remote methods for a distributed library system.
 * This interface extends Remote, allowing it to be used in an RMI-based application.
 */
public interface Library extends Remote {

    List<String> getConnectedPeers() throws RemoteException;

    void registerPeer(PeerInfo peer) throws RemoteException;

    void deregisterPeer(PeerInfo peer) throws RemoteException;

    List<String> searchSongs(String query) throws RemoteException;

    byte[] streamSong(String songName) throws RemoteException;

    String getSongMetadata(String songName) throws RemoteException;

    void rateSong(String songName, int rating) throws RemoteException;
}

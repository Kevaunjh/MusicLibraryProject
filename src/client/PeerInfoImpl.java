package client;

import common.PeerInfo;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Implementation of the PeerInfo interface for RMI communication.
 * This class extends UnicastRemoteObject to allow it to be used as an RMI remote object.
 */
public class PeerInfoImpl extends UnicastRemoteObject implements PeerInfo {
    // The address of the peer (e.g., IP and port).
    private final String peerAddress;
    // The playlist associated with the peer.
    private final List<String> playlist;


    public PeerInfoImpl(String peerAddress, List<String> playlist) throws RemoteException {
        super(); // Calls the UnicastRemoteObject constructor to export this object for remote use.
        this.peerAddress = peerAddress;
        this.playlist = playlist;
    }

    @Override
    public String getPeerAddress() throws RemoteException {
        return peerAddress;
    }

    @Override
    public List<String> getPlaylist() throws RemoteException {
        return playlist;
    }
}

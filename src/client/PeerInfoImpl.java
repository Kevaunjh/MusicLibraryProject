package client;

import common.PeerInfo;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class PeerInfoImpl extends UnicastRemoteObject implements PeerInfo {
    private final String peerAddress;
    private final List<String> playlist;

    public PeerInfoImpl(String peerAddress, List<String> playlist) throws RemoteException {
        super();
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

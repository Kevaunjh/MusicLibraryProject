package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PeerInfo extends Remote {
    String getPeerAddress() throws RemoteException;

    List<String> getPlaylist() throws RemoteException;
}

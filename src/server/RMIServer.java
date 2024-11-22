package server;

import common.PeerInfo;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIServer extends Remote {
    void registerPeer(PeerInfo peer) throws RemoteException;

    void deregisterPeer(PeerInfo peer) throws RemoteException;

    List<PeerInfo> getConnectedPeers() throws RemoteException;
}

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Library extends Remote {
    List<String> searchSongs(String query) throws RemoteException;

    byte[] streamSong(String songName) throws RemoteException;

    boolean uploadSong(String songName, byte[] data) throws RemoteException;

    String getSongMetadata(String songName) throws RemoteException;

    boolean rateSong(String songName, int rating) throws RemoteException;
}

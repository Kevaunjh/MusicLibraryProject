package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Server {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            RMIServerImpl musicLibrary = new RMIServerImpl();
            Naming.rebind("//localhost/Library", musicLibrary);

            System.out.println("Music Library Server is running...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

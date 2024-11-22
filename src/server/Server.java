package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Server {
    public static void main(String[] args) {
        try {
            // Start the RMI registry on port 1099
            LocateRegistry.createRegistry(1099);

            // Create and bind the RMIServer implementation
            RMIServerImpl musicLibrary = new RMIServerImpl();
            Naming.rebind("//localhost/Library", musicLibrary);

            System.out.println("Music Library Server is running...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

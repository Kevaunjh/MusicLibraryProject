package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Server {
    /**
     * The main method to start the RMI server.
     * It sets up the RMI registry, creates an instance of the RMIServerImpl class,
     * and binds it to the registry for remote access.
     */
    public static void main(String[] args) {
        try {
            // Create the RMI registry on port 1099 (default RMI registry port).
            LocateRegistry.createRegistry(1099);

            // Create an instance of RMIServerImpl, which implements the Library interface.
            RMIServerImpl musicLibrary = new RMIServerImpl();

            // Bind the created RMIServerImpl instance to the RMI registry with the name "Library".
            Naming.rebind("//localhost/Library", musicLibrary);

            // Print confirmation message that the server is running.
            System.out.println("Music Library Server is running...");
        } catch (Exception e) {
            // Handle any exceptions that may occur during server setup or binding.
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

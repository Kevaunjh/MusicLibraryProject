import java.rmi.Naming;

public class Server {
    public static void main(String[] args) {
        try {
            LibraryImpl musicLibrary = new LibraryImpl();
            Naming.rebind("//localhost/Library", musicLibrary);
            System.out.println("Music Library Server is running...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

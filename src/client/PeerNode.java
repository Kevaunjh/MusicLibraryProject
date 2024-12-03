import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Represents a peer node in a P2P network.
 * This class handles file sharing, file downloads, and server operations for the peer.
 */
public class PeerNode {
    private final InetSocketAddress address; // The address of the peer node.
    private final P2PProtocol protocol; // The protocol used for file registration and discovery.
    private final List<String> localFiles; // List of files hosted by this peer.

    public PeerNode(InetSocketAddress address, P2PProtocol protocol) {
        this.address = address;
        this.protocol = protocol;
        this.localFiles = new ArrayList<>();
    }

    /**
     * Starts a server to listen for incoming file requests from clients.
     * Runs in a separate thread to handle connections concurrently.
     */
    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(address.getPort())) {
                System.out.println("Peer server started at: " + address);

                // Continuously listen for incoming client connections.
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    // For each client connection, create a new thread to handle file transfer.
                    new Thread(new FileTransferHandler(clientSocket, localFiles)).start();
                }
            } catch (IOException e) {
                System.err.println("Error starting server: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Registers the local files with the protocol, making them available for other peers to discover.
     */
    public void registerFiles() {
        protocol.registerPeer(address, localFiles);
        System.out.println("Files registered with the protocol: " + localFiles);
    }

    public void addFile(String fileName) {
        File file = new File("Songs/" + fileName);
        if (file.exists()) {
            localFiles.add(fileName);
            protocol.registerPeer(address, Collections.singletonList(fileName));
            System.out.println("File added: " + fileName);
        } else {
            System.err.println("File does not exist: " + fileName);
        }
    }

    public void downloadFile(String fileName) {
        InetSocketAddress sourcePeer = protocol.discoverFile(fileName);
        if (sourcePeer == null) {
            System.out.println("File not found in the network: " + fileName);
            return;
        }

        // Establish a connection to the peer that hosts the file and download it.
        try (Socket socket = new Socket(sourcePeer.getAddress(), sourcePeer.getPort());
             InputStream is = socket.getInputStream();
             FileOutputStream fos = new FileOutputStream("Downloads/" + fileName)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            System.out.println("File downloaded successfully: " + fileName);
        } catch (IOException e) {
            System.err.println("Error downloading file: " + e.getMessage());
        }
    }

    /**
     * Displays the list of local files hosted by this peer.
     */
    public void displayLocalFiles() {
        System.out.println("Local files hosted by this peer: " + localFiles);
    }

    /**
     * Handles incoming client connections for file transfer.
     * Implements Runnable to be executed in a separate thread.
     */
    private static class FileTransferHandler implements Runnable {
        private final Socket clientSocket; // The client socket to communicate with the client.
        private final List<String> localFiles; // The list of local files available for transfer.

        public FileTransferHandler(Socket clientSocket, List<String> localFiles) {
            this.clientSocket = clientSocket;
            this.localFiles = localFiles;
        }

        /**
         * Runs the file transfer operation.
         * Listens for file requests, checks if the file is available, and sends it if found.
         */
        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // Read the requested file name from the client.
                String requestedFile = reader.readLine();
                if (localFiles.contains(requestedFile)) {
                    File file = new File("Songs/" + requestedFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    // Send the file content to the client.
                    try (FileInputStream fis = new FileInputStream(file)) {
                        OutputStream os = clientSocket.getOutputStream();
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        os.flush();
                        System.out.println("File sent successfully: " + requestedFile);
                    }
                } else {
                    System.out.println("Requested file not found: " + requestedFile);
                    writer.println("ERROR: File not found");
                }
            } catch (IOException e) {
                System.err.println("Error handling file transfer: " + e.getMessage());
            }
        }
    }
}

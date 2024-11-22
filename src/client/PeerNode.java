import java.io.*;
import java.net.*;
import java.util.*;

public class PeerNode {
    private final InetSocketAddress address; // Address of this peer
    private final P2PProtocol protocol;
    private final List<String> localFiles; // Files hosted by this peer

    public PeerNode(InetSocketAddress address, P2PProtocol protocol) {
        this.address = address;
        this.protocol = protocol;
        this.localFiles = new ArrayList<>();
    }

    // Start the peer server to handle incoming file requests
    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(address.getPort())) {
                System.out.println("Peer server started at: " + address);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new FileTransferHandler(clientSocket, localFiles)).start();
                }
            } catch (IOException e) {
                System.err.println("Error starting server: " + e.getMessage());
            }
        }).start();
    }

    // Register this peer with the protocol
    public void registerFiles() {
        protocol.registerPeer(address, localFiles);
        System.out.println("Files registered with the protocol: " + localFiles);
    }

    // Add a file to the local repository
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

    // Download a file from another peer
    public void downloadFile(String fileName) {
        InetSocketAddress sourcePeer = protocol.discoverFile(fileName);
        if (sourcePeer == null) {
            System.out.println("File not found in the network: " + fileName);
            return;
        }

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

    // Display the list of local files
    public void displayLocalFiles() {
        System.out.println("Local files hosted by this peer: " + localFiles);
    }

    // Handler for incoming file requests
    private static class FileTransferHandler implements Runnable {
        private final Socket clientSocket;
        private final List<String> localFiles;

        public FileTransferHandler(Socket clientSocket, List<String> localFiles) {
            this.clientSocket = clientSocket;
            this.localFiles = localFiles;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String requestedFile = reader.readLine();
                if (localFiles.contains(requestedFile)) {
                    File file = new File("Songs/" + requestedFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;

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

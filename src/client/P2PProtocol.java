import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class P2PProtocol {
    private final Map<String, InetSocketAddress> fileIndex = new ConcurrentHashMap<>();
    private final List<InetSocketAddress> peers = Collections.synchronizedList(new ArrayList<>());

    // Register a peer and the files it hosts
    public synchronized void registerPeer(InetSocketAddress peer, List<String> files) {
        if (!peers.contains(peer)) {
            peers.add(peer);
            System.out.println("Peer registered: " + peer);
        }
        for (String file : files) {
            fileIndex.put(file, peer);
            System.out.println("File registered: " + file + " at " + peer);
        }
    }

    // Get a list of connected peers
    public List<InetSocketAddress> getPeers() {
        return new ArrayList<>(peers);
    }

    // Discover a file and return the peer hosting it
    public InetSocketAddress discoverFile(String fileName) {
        InetSocketAddress peer = fileIndex.get(fileName);
        if (peer == null) {
            System.out.println("File not found in the network: " + fileName);
        } else {
            System.out.println("File " + fileName + " found at " + peer);
        }
        return peer;
    }

    // Download a file from either a local source or a peer
    public void downloadFile(InetSocketAddress sourcePeer, String fileName, String saveDirectory) {
        try {
            File saveDir = new File(saveDirectory);
            if (!saveDir.exists())
                saveDir.mkdirs(); // Create the save directory if it doesn't exist

            // Remove the first occurrence of "Songs/" from the path if present
            String sanitizedFileName = fileName.replaceFirst("^.*?Songs/", "");

            File targetFile = new File(saveDir, sanitizedFileName); // Target file in the CurrentClientDownloads folder

            if (sourcePeer == null) { // Local file download
                File localFile = new File("../../Songs/" + sanitizedFileName); // Use the sanitized path
                if (!localFile.exists()) {
                    System.err.println("Error: File not found locally: " + localFile.getAbsolutePath());
                    return;
                }
                try (InputStream is = new FileInputStream(localFile);
                        OutputStream os = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                System.out.println("File downloaded locally: " + targetFile.getAbsolutePath());
            } else { // Peer-to-peer file download
                try (Socket socket = new Socket(sourcePeer.getAddress(), sourcePeer.getPort());
                        InputStream is = socket.getInputStream();
                        OutputStream os = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                System.out.println("File downloaded from peer: " + targetFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error downloading file: " + e.getMessage());
        }
    }

    // Simulated method to act as a server to share files (peer-to-peer file
    // serving)
    public void serveFiles(int port, String fileDirectory) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("File server started on port: " + port);
                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                            InputStream fileInputStream = new FileInputStream(fileDirectory);
                            OutputStream clientOutputStream = clientSocket.getOutputStream()) {

                        System.out.println("Connected to client: " + clientSocket.getInetAddress());

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            clientOutputStream.write(buffer, 0, bytesRead);
                        }

                        System.out.println("File sent successfully to client.");
                    } catch (IOException e) {
                        System.err.println("Error serving file: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Error starting file server: " + e.getMessage());
            }
        }).start();
    }
}

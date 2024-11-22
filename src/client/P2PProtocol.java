package client;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class P2PProtocol {
    private final Map<String, InetSocketAddress> fileIndex = new ConcurrentHashMap<>();
    private final List<InetSocketAddress> peers = Collections.synchronizedList(new ArrayList<>());

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

    public List<InetSocketAddress> getPeers() {
        return new ArrayList<>(peers);
    }

    public InetSocketAddress discoverFile(String fileName) {
        InetSocketAddress peer = fileIndex.get(fileName);
        if (peer == null) {
            System.out.println("File not found in the network: " + fileName);
        } else {
            System.out.println("File " + fileName + " found at " + peer);
        }
        return peer;
    }

    public void downloadFile(InetSocketAddress sourcePeer, String fileName, String saveDirectory) {
        try {
            File saveDir = new File(saveDirectory);
            if (!saveDir.exists())
                saveDir.mkdirs();

            String sanitizedFileName = fileName.replaceFirst("^.*?Songs/", "");

            File targetFile = new File(saveDir, sanitizedFileName);

            if (sourcePeer == null) {
                File localFile = new File("../Songs/" + sanitizedFileName);
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
            } else {
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

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class FileServer {

    private static final int PORT = 5000;
    private static final String STORAGE_DIR = "server_files/";
    private ServerSocket serverSocket;
    private ExecutorService threadPool;

    public FileServer() {
        threadPool = Executors.newFixedThreadPool(10);
    }

    public void start() {
        File storageDir = new File(STORAGE_DIR);
        if (!storageDir.exists()) {
            storageDir.mkdir();
            System.out.println("[Server] Created storage directory: " + STORAGE_DIR);
        }

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("[Server] Started on port " + PORT);
            System.out.println("[Server] Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] New client connected: "
                        + clientSocket.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(clientSocket, STORAGE_DIR);
                threadPool.execute(handler);
            }

        } catch (IOException e) {
            System.err.println("[Server] Error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            System.out.println("[Server] Shut down.");
        } catch (IOException e) {
            System.err.println("[Server] Error during shutdown: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        FileServer server = new FileServer();
        server.start();
    }
}
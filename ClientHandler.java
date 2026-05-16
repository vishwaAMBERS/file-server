import java.io.*;
import java.net.*;

/**
 * ClientHandler.java
 * Runs in its own thread. Handles one client connection.
 *
 * Supported Commands from client:
 *   UPLOAD <filename>    -> client sends file to server
 *   DOWNLOAD <filename>  -> server sends file to client
 *   LIST                 -> server lists all files in storage
 *   DELETE <filename>    -> server deletes a file
 *   EXIT                 -> client disconnects
 */
public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private String storageDir;

    // Streams for text commands
    private BufferedReader reader;
    private PrintWriter writer;

    // Streams for binary file data
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    public ClientHandler(Socket socket, String storageDir) {
        this.clientSocket = socket;
        this.storageDir = storageDir;
    }

    @Override
    public void run() {
        try {
            // Set up text I/O for commands
            reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()), true);

            // Set up binary I/O for file transfers
            dataIn  = new DataInputStream(clientSocket.getInputStream());
            dataOut = new DataOutputStream(clientSocket.getOutputStream());

            writer.println("Welcome to FileServer! Commands: UPLOAD, DOWNLOAD, LIST, DELETE, EXIT");

            String command;
            // Read commands line by line until client disconnects or sends EXIT
            while ((command = reader.readLine()) != null) {
                command = command.trim();
                System.out.println("[Handler] Received command: " + command);

                if (command.equalsIgnoreCase("EXIT")) {
                    writer.println("Goodbye!");
                    break;

                } else if (command.equalsIgnoreCase("LIST")) {
                    handleList();

                } else if (command.toUpperCase().startsWith("UPLOAD ")) {
                    String filename = command.substring(7).trim();
                    handleUpload(filename);

                } else if (command.toUpperCase().startsWith("DOWNLOAD ")) {
                    String filename = command.substring(9).trim();
                    handleDownload(filename);

                } else if (command.toUpperCase().startsWith("DELETE ")) {
                    String filename = command.substring(7).trim();
                    handleDelete(filename);

                } else {
                    writer.println("ERROR: Unknown command: " + command);
                }
            }

        } catch (IOException e) {
            System.err.println("[Handler] Client disconnected: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    // ─────────────────────────────────────────────
    // LIST: send all filenames in storage directory
    // ─────────────────────────────────────────────
    private void handleList() {
        File dir = new File(storageDir);
        File[] files = dir.listFiles();

        if (files == null || files.length == 0) {
            writer.println("No files found.");
        } else {
            writer.println("Files on server (" + files.length + "):");
            for (File f : files) {
                // Send each filename with its size
                writer.println("  " + f.getName() + " (" + f.length() + " bytes)");
            }
        }
        writer.println("END_LIST"); // sentinel so client knows list is done
    }

    // ─────────────────────────────────────────────
    // UPLOAD: receive a file from the client
    // Protocol: client sends file size (long), then raw bytes
    // ─────────────────────────────────────────────
    private void handleUpload(String filename) throws IOException {
        // Sanitize filename to prevent path traversal attack (e.g., ../../etc/passwd)
        filename = new File(filename).getName();

        writer.println("READY"); // tell client we are ready to receive

        // Step 1: Read file size
        long fileSize = dataIn.readLong();

        // Step 2: Read file bytes and write to disk
        File outputFile = new File(storageDir + filename);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            long remaining = fileSize;

            while (remaining > 0) {
                int bytesToRead = (int) Math.min(buffer.length, remaining);
                int bytesRead = dataIn.read(buffer, 0, bytesToRead);
                if (bytesRead == -1) break;
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }

        System.out.println("[Handler] Uploaded: " + filename + " (" + fileSize + " bytes)");
        writer.println("SUCCESS: " + filename + " uploaded (" + fileSize + " bytes)");
    }

    // ─────────────────────────────────────────────
    // DOWNLOAD: send a file to the client
    // Protocol: server sends file size (long), then raw bytes
    // ─────────────────────────────────────────────
    private void handleDownload(String filename) throws IOException {
        filename = new File(filename).getName(); // sanitize

        File file = new File(storageDir + filename);

        if (!file.exists()) {
            writer.println("ERROR: File not found: " + filename);
            return;
        }

        writer.println("READY"); // signal client to prepare for data

        // Step 1: Send file size so client knows how many bytes to read
        dataOut.writeLong(file.length());
        dataOut.flush();

        // Step 2: Send file bytes
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
            }
            dataOut.flush();
        }

        System.out.println("[Handler] Downloaded: " + filename + " (" + file.length() + " bytes)");
        writer.println("SUCCESS: " + filename + " downloaded");
    }

    // ─────────────────────────────────────────────
    // DELETE: remove a file from storage
    // ─────────────────────────────────────────────
    private void handleDelete(String filename) {
        filename = new File(filename).getName(); // sanitize
        File file = new File(storageDir + filename);

        if (!file.exists()) {
            writer.println("ERROR: File not found: " + filename);
        } else if (file.delete()) {
            System.out.println("[Handler] Deleted: " + filename);
            writer.println("SUCCESS: " + filename + " deleted");
        } else {
            writer.println("ERROR: Could not delete " + filename);
        }
    }

    // ─────────────────────────────────────────────
    // Clean up streams and socket
    // ─────────────────────────────────────────────
    private void closeConnection() {
        try {
            if (reader  != null) reader.close();
            if (writer  != null) writer.close();
            if (dataIn  != null) dataIn.close();
            if (dataOut != null) dataOut.close();
            if (clientSocket != null && !clientSocket.isClosed())
                clientSocket.close();
            System.out.println("[Handler] Connection closed.");
        } catch (IOException e) {
            System.err.println("[Handler] Error closing: " + e.getMessage());
        }
    }
}
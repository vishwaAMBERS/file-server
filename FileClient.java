import java.io.*;
import java.net.*;

public class FileClient {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 5000;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    public void connect() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("[Client] Connected to server at " + SERVER_IP + ":" + SERVER_PORT);

            reader  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer  = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            dataIn  = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());

            // Print welcome message from server
            System.out.println("[Server] " + reader.readLine());

            // Start taking user input
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String command;

            System.out.print("\nEnter command: ");
            while ((command = userInput.readLine()) != null) {
                writer.println(command);

                if (command.equalsIgnoreCase("EXIT")) {
                    System.out.println("[Server] " + reader.readLine());
                    break;

                } else if (command.equalsIgnoreCase("LIST")) {
                    String line;
                    while (!(line = reader.readLine()).equals("END_LIST")) {
                        System.out.println(line);
                    }

                } else if (command.toUpperCase().startsWith("UPLOAD ")) {
                    String filename = command.substring(7).trim();
                    uploadFile(filename);

                } else if (command.toUpperCase().startsWith("DOWNLOAD ")) {
                    String filename = command.substring(9).trim();
                    downloadFile(filename);

                } else {
                    // For DELETE and others, just print server response
                    System.out.println("[Server] " + reader.readLine());
                }

                System.out.print("\nEnter command: ");
            }

        } catch (IOException e) {
            System.err.println("[Client] Error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void uploadFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("[Client] File not found: " + filename);
            return;
        }

        String response = reader.readLine(); // wait for READY
        if (!response.equals("READY")) {
            System.out.println("[Server] " + response);
            return;
        }

        // Send file size first
        dataOut.writeLong(file.length());
        dataOut.flush();

        // Send file bytes
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
            }
            dataOut.flush();
        }

        System.out.println("[Server] " + reader.readLine());
    }

    private void downloadFile(String filename) throws IOException {
        String response = reader.readLine(); // wait for READY or ERROR
        if (response.startsWith("ERROR")) {
            System.out.println("[Server] " + response);
            return;
        }

        // Read file size
        long fileSize = dataIn.readLong();

        // Receive file bytes and save
        try (FileOutputStream fos = new FileOutputStream("downloaded_" + filename)) {
            byte[] buffer = new byte[4096];
            long remaining = fileSize;
            while (remaining > 0) {
                int bytesRead = dataIn.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (bytesRead == -1) break;
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }

        System.out.println("[Client] File saved as: downloaded_" + filename);
        System.out.println("[Server] " + reader.readLine());
    }

    private void disconnect() {
        try {
            if (socket != null) socket.close();
            System.out.println("[Client] Disconnected.");
        } catch (IOException e) {
            System.err.println("[Client] Error disconnecting: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        FileClient client = new FileClient();
        client.connect();
    }
}
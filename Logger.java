import java.io.*;
import java.time.*;
import java.time.format.*;

public class Logger {

    private static final String LOG_FILE = "server.log";
    private static Logger instance;   // Singleton pattern
    private PrintWriter writer;

    // Private constructor - only one Logger object allowed
    private Logger() {
        try {
            // true = append mode (don't overwrite old logs)
            writer = new PrintWriter(new FileWriter(LOG_FILE, true));
            System.out.println("[Logger] Logging to: " + LOG_FILE);
        } catch (IOException e) {
            System.err.println("[Logger] Error: " + e.getMessage());
        }
    }

    // Get single instance (Singleton)
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    // Log any message with timestamp
    public void log(String message) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = "[" + timestamp + "] " + message;
        System.out.println(logEntry);       // print to console
        writer.println(logEntry);           // write to file
        writer.flush();                     // save immediately
    }

    // Log with client info
    public void log(String clientIP, String action, String filename) {
        log("CLIENT: " + clientIP + " | ACTION: " + action + " | FILE: " + filename);
    }

    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
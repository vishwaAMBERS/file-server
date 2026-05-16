import java.io.*;
import java.util.*;

public class AuthManager {

    private static final String USERS_FILE = "users.txt";
    private Map<String, String> users = new HashMap<>();

    public AuthManager() {
        loadUsers();
    }

    // Read users from users.txt file
    private void loadUsers() {
        File file = new File(USERS_FILE);

        // If users.txt doesn't exist, create default users
        if (!file.exists()) {
            createDefaultUsers();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0].trim(), parts[1].trim());
                }
            }
            System.out.println("[Auth] Loaded " + users.size() + " users.");
        } catch (IOException e) {
            System.err.println("[Auth] Error loading users: " + e.getMessage());
        }
    }

    // Create a default users.txt with 3 users
    private void createDefaultUsers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            pw.println("admin:admin123");
            pw.println("user1:pass1");
            pw.println("user2:pass2");
            System.out.println("[Auth] Created default users.txt");
        } catch (IOException e) {
            System.err.println("[Auth] Error creating users file: " + e.getMessage());
        }
    }

    // Check if username and password match
    public boolean authenticate(String username, String password) {
        if (users.containsKey(username)) {
            return users.get(username).equals(password);
        }
        return false;
    }

    // Add new user
    public boolean addUser(String username, String password) {
        if (users.containsKey(username)) {
            return false; // user already exists
        }
        users.put(username, password);
        saveUsers();
        return true;
    }

    // Save all users back to file
    private void saveUsers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                pw.println(entry.getKey() + ":" + entry.getValue());
            }
        } catch (IOException e) {
            System.err.println("[Auth] Error saving users: " + e.getMessage());
        }
    }
}
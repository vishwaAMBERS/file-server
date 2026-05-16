# 🖥️ Multi-Threaded File Server — Java Socket Programming

A client-server file transfer application built using **Core Java**.  
Supports multiple simultaneous clients, file upload/download, user authentication, and activity logging.

---

## 📁 Project Structure

```
FileServerProject/
├── FileServer.java        # Server entry point — listens on port 5000
├── ClientHandler.java     # Handles each client in a separate thread
├── FileClient.java        # Client application — sends commands to server
├── AuthManager.java       # User login system — reads from users.txt
├── Logger.java            # Logs all activity to server.log (Singleton)
├── users.txt              # Auto-created — stores username:password
├── server.log             # Auto-created — stores all activity logs
└── server_files/          # Auto-created — stores uploaded files
```

---

## ⚙️ Requirements

| Item | Version |
|------|---------|
| Java JDK | 17 or higher |
| OS | Windows / Linux / macOS |
| IDE (optional) | VS Code, IntelliJ IDEA |

---

## 🚀 How to Run

### Step 1 — Go to Project Folder
```bash
cd C:\Users\Lenovo\Desktop\zoho\FileServerProject
```

### Step 2 — Compile All Files
```bash
javac *.java
```

### Step 3 — Start the Server (Terminal 1)
```bash
java FileServer
```
Expected output:
```
[Server] Created storage directory: server_files/
[Server] Started on port 5000
[Server] Waiting for clients...
```

### Step 4 — Start the Client (Terminal 2)
```bash
java FileClient
```
Expected output:
```
[Client] Connected to server at localhost:5000
[Server] Welcome to FileServer! Commands: UPLOAD, DOWNLOAD, LIST, DELETE, EXIT

Enter command:
```

---

## 💬 Commands

| Command | Format | Description |
|---------|--------|-------------|
| `LIST` | `LIST` | Show all files stored on server |
| `UPLOAD` | `UPLOAD filename.txt` | Send a file from client to server |
| `DOWNLOAD` | `DOWNLOAD filename.txt` | Get a file from server to client |
| `DELETE` | `DELETE filename.txt` | Remove a file from server |
| `EXIT` | `EXIT` | Disconnect from server |

---

## 🧪 Testing Example

```
Enter command: LIST
No files found.

Enter command: UPLOAD test.txt
[Server] SUCCESS: test.txt uploaded (27 bytes)

Enter command: LIST
Files on server (1):
  test.txt (27 bytes)

Enter command: DOWNLOAD test.txt
[Client] File saved as: downloaded_test.txt

Enter command: DELETE test.txt
[Server] SUCCESS: test.txt deleted

Enter command: EXIT
Goodbye!
```

---

## 🔐 Default Login Credentials

Stored in `users.txt` (auto-created on first run):

```
admin:admin123
user1:pass1
user2:pass2
```

---

## 📋 Log File Sample

`server.log` is created automatically and logs every action:

```
[2024-11-15 10:30:00] CLIENT: 127.0.0.1 | ACTION: UPLOAD | FILE: test.txt
[2024-11-15 10:30:10] CLIENT: 127.0.0.1 | ACTION: DOWNLOAD | FILE: test.txt
[2024-11-15 10:30:20] CLIENT: 127.0.0.1 | ACTION: DELETE | FILE: test.txt
```

---

## 🏗️ Key Concepts Used

| Concept | Where Used |
|---------|------------|
| Socket Programming | `ServerSocket`, `Socket` in FileServer & FileClient |
| Multi-threading | `ExecutorService` thread pool — one thread per client |
| Binary File Transfer | `DataInputStream` / `DataOutputStream` |
| Text Commands | `BufferedReader` / `PrintWriter` |
| File Handling | `FileInputStream` / `FileOutputStream` |
| Design Pattern | Singleton Pattern in `Logger.java` |
| Security | Path traversal prevention in `ClientHandler.java` |

---

## 📌 How It Works

```
Client                        Server
  |                              |
  |--- Connect (port 5000) ----> |
  |<-- Welcome message --------- |
  |                              |
  |--- UPLOAD filename --------> |  ClientHandler thread starts
  |<-- READY ------------------- |
  |--- File size (long) -------> |
  |--- File bytes (binary) ----> |
  |<-- SUCCESS ----------------- |
  |                              |
  |--- EXIT -------------------> |
  |<-- Goodbye ----------------- |
  |                              |  Thread ends, socket closed
```

---

## ⚠️ Common Errors & Fixes

| Error | Cause | Fix |
|-------|-------|-----|
| `ClassNotFoundException` | Wrong folder | Run `cd` to project folder first |
| `duplicate class` | Old `.class` files | Run `del *.class` then recompile |
| `Connection refused` | Server not running | Start `java FileServer` first |
| `File not found` | Wrong filename | Check spelling, file must exist locally |
| `cannot find symbol` | Missing imports | Add `import java.io.*;` `import java.net.*;` |

---

## 📚 References

- Java SE 17 Documentation — https://docs.oracle.com
- Socket Programming — https://www.javatpoint.com/socket-programming
- Java Multi-threading — https://www.geeksforgeeks.org/multithreading-in-java
- Computer Networks — Tanenbaum (5th Edition)
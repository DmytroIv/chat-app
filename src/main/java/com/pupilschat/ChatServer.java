package com.pupilschat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChatServer {

    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        int port = 12345;
        DatabaseManager.initializeDatabase(); // Initialize the database when the server starts

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started. Listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Create a new thread to handle the client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    // method sends a message to all connected clients
    public static void broadcast(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }

    // method to add writer to the set
    public static void addWriter(PrintWriter writer) {
        clientWriters.add(writer);
    }

    // method to remove writer from the set
    public static void removeWriter(PrintWriter writer) {
        clientWriters.remove(writer);
    }
}
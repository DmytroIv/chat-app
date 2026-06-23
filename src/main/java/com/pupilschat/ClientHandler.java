package com.pupilschat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Init the output stream to send messages back to the client
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            // Add the writer to the server's set of writers
            ChatServer.addWriter(out);

            // get the client's IP to store it in the db
            String clientAddress = clientSocket.getInetAddress().getHostAddress();

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
                // save the message to the DB
                DatabaseManager.saveMessage(clientAddress, message);

                // broadcast the message
                ChatServer.broadcast("Users message: " + message);
            }

        } catch (IOException e) {
            System.err.println("Error occurred while handling client: " + e.getMessage());
        } finally {

            // Clean up: remove the writer from the server's set when the client disconnects
            if (out != null) {
                ChatServer.removeWriter(out);
            }

            // Close the client socket when done
            try {
                clientSocket.close();
                System.out.println("Client connection closed.");
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}

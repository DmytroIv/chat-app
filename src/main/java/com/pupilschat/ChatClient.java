package com.pupilschat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int port = 9090;

        try {
            Socket socket = new Socket(serverAddress, port);
            System.out.println("Successfully connected to the server!");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner keyboardScanner = new Scanner(System.in);

            // Create a thread to listen for messages from the server
            Thread listenerThread = new Thread(() -> {
                try {
                    String incomingMessage;
                    while ((incomingMessage = in.readLine()) != null) {
                        System.out.println("in :" + incomingMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });

            listenerThread.start();
            System.out.print("Enter a message to send (or type 'exit' or 'quit' to quit): ");

            // Handles your typing
            while (true) {
                String message = keyboardScanner.nextLine();

                if (message.equalsIgnoreCase("quit") || message.equalsIgnoreCase("exit")) {
                    break;
                }

                out.println(message);
            }

            keyboardScanner.close();
            socket.close();
            System.out.println("Connection closed.");

        } catch (IOException e) {
            System.err.println("Could not connect to the server: " + e.getMessage());
        }
    }
}

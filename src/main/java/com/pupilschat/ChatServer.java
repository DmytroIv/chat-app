package com.pupilschat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatServer {

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        SpringApplication.run(ChatServer.class, args);
        System.out.println("Pupil Chat API is online and waiting for WebSockets!");
    }
}
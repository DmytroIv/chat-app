package com.pupilschat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.pupilschat.service.DatabaseManager;

@SpringBootApplication
public class ChatServer {

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        SpringApplication.run(ChatServer.class, args);
    }
}
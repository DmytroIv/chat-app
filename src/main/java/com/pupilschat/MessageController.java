package com.pupilschat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // Tells Spring this class handles HTTP REST API requests
@RequestMapping("/api/messages") // The base URL for this controller
public class MessageController {

    // This listens for HTTP GET requests at http://localhost:8080/api/messages
    @GetMapping
    public List<Message> getChatHistory() {
        // Fetch the list of messages from the DB
        // Spring Boot will automatically convert this List into a JSON array!
        return DatabaseManager.getAllMessages();
    }
}

package com.pupilschat;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // Tells Spring this class handles HTTP REST API requests
@RequestMapping("/api/messages") // The base URL for this controller
public class MessageController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // This listens for HTTP GET requests at http://localhost:8080/api/messages
    @GetMapping
    public List<Message> getChatHistory() {
        // Fetch the list of messages from the DB
        // Spring Boot will automatically convert this List into a JSON array!
        return DatabaseManager.getAllMessages();
    }

    // Endpoint to receive a message from the web
    @PostMapping
    public String receiveWebMessage(@RequestBody Message incomingMessage) {
        // Package the message into a simple string payload
        String payload = incomingMessage.getSender() + "|||" + incomingMessage.getContent();

        // Send it to the RabbitMQ Post Office!
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, payload);

        return "Message successfully sent to RabbitMQ!";
    }
}

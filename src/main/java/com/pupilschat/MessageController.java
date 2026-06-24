package com.pupilschat;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Tells Spring this class handles HTTP REST API requests
@RequestMapping("/api/messages") // The base URL for this controller
public class MessageController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // We now look for a specific room in the URL (e.g., /api/messages/engineering)
    @GetMapping("/{room}")
    public List<Message> getChatHistory(@PathVariable String room) {
        return DatabaseManager.getMessagesByRoom(room);
    }

    @PostMapping
    public String receiveWebMessage(@RequestBody Message incomingMessage) {
        // Package the room into the payload!
        String payload = incomingMessage.getRoom() + "|||" + incomingMessage.getSender() + "|||"
                + incomingMessage.getContent();

        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, payload);
        return "Message successfully sent to RabbitMQ!";
    }
}

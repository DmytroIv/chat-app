package com.pupilschat;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/{room}")
    public List<Message> getChatHistory(@PathVariable String room) {
        return DatabaseManager.getMessagesByRoom(room);
    }

    @PostMapping
    public String receiveWebMessage(@RequestBody Message incomingMessage) {
        String payload = incomingMessage.getRoom() + "|||" + incomingMessage.getSender() + "|||"
                + incomingMessage.getContent();

        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, payload);
        return "Message successfully sent to RabbitMQ!";
    }
}

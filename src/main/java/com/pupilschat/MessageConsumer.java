package com.pupilschat;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeMessageFromQueue(String payload) {
        System.out.println("📦 RabbitMQ Consumer grabbed a message: " + payload);

        // Unpack the string payload
        String[] parts = payload.split("\\|\\|\\|");
        if (parts.length == 2) {
            String sender = parts[0];
            String content = parts[1];

            // 1. Save it to our PostgreSQL Database
            DatabaseManager.saveMessage(sender, content);

            // 2. Broadcast it to all connected TCP Desktop Clients
            Message outMessage = new Message(sender, content);
            messagingTemplate.convertAndSend("/topic/messages", outMessage);
        }
    }
}

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
        System.out.println("RabbitMQ routing message: " + payload);
        String[] parts = payload.split("\\|\\|\\|");
        if (parts.length == 3) {
            String room = parts[0];
            String sender = parts[1];
            String content = parts[2];

            DatabaseManager.saveMessage(room, sender, content);

            Message outMessage = new Message(room, sender, content);
            messagingTemplate.convertAndSend("/topic/" + room, outMessage);

            System.out.println("Successfully broadcasted to /topic/" + room);
        } else {
            System.err.println(
                    "ERROR: Invalid payload received! Expected 3 parts but got: " + parts.length + " -> " + payload);
        }
    }
}

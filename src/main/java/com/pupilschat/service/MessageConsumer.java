package com.pupilschat.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.pupilschat.config.RabbitMQConfig;
import com.pupilschat.model.Message;

@Component
public class MessageConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeMessageFromQueue(String payload) {

        String[] parts = payload.split("\\|\\|\\|");

        if (parts.length == 4) {
            String room = parts[0];
            String sender = parts[1];
            String content = parts[2];
            String time = parts[3];

            DatabaseManager.saveMessage(room, sender, content);

            Message outMessage = new Message(room, sender, content, time);
            messagingTemplate.convertAndSend("/topic/" + room, outMessage);

        } else {
            System.err
                    .println("❌ ERROR: Invalid payload! Expected 4 parts but got: " + parts.length + " -> " + payload);
        }
    }
}

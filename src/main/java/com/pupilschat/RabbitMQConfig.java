package com.pupilschat;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "chat_queue";

    @Bean
    public Queue chatQueue() {
        return new Queue(QUEUE_NAME, true);
    }
}

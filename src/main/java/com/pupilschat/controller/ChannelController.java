package com.pupilschat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import com.pupilschat.service.DatabaseManager;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public List<String> getChannels() {
        return DatabaseManager.getAllChannels();
    }

    @PostMapping("/{name}")
    public String createChannel(@PathVariable String name) {
        boolean success = DatabaseManager.createChannel(name);
        if (success) {
            messagingTemplate.convertAndSend("/topic/channels", "NEW_CHANNEL_CREATED");
            return "SUCCESS";
        }
        return "ERROR";
    }
}

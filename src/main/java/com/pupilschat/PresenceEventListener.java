package com.pupilschat;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class PresenceEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    public PresenceEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/api/presence")
    public Set<String> getActiveUsers() {
        return Set.copyOf(sessionUserMap.values());
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String username = accessor.getFirstNativeHeader("username");

        if (username != null) {
            sessionUserMap.put(sessionId, username);
            broadcastPresence();
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionUserMap.containsKey(sessionId)) {
            sessionUserMap.remove(sessionId);
            broadcastPresence();
        }
    }

    private void broadcastPresence() {
        Set<String> uniqueUsers = Set.copyOf(sessionUserMap.values());
        messagingTemplate.convertAndSend("/topic/presence", uniqueUsers);
    }
}

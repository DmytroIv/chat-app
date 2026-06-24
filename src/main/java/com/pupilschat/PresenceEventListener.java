package com.pupilschat;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PresenceEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    public PresenceEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        // Extract the custom 'username' header we will send from JavaScript
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

        // If the session disconnects (e.g., closing the tab), remove them and broadcast
        // the update
        if (sessionUserMap.containsKey(sessionId)) {
            sessionUserMap.remove(sessionId);
            broadcastPresence();
        }
    }

    private void broadcastPresence() {
        // Grab the unique list of online users
        Set<String> uniqueUsers = Set.copyOf(sessionUserMap.values());

        // Broadcast the list to a new global channel: /topic/presence
        messagingTemplate.convertAndSend("/topic/presence", uniqueUsers);
    }
}

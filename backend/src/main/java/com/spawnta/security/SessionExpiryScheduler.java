package com.spawnta.security;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SessionExpiryScheduler {

    private final ConnectionManager connectionManager;
    private final SimpMessagingTemplate messagingTemplate;

    public SessionExpiryScheduler(ConnectionManager connectionManager, SimpMessagingTemplate messagingTemplate) {
        this.connectionManager = connectionManager;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void checkExpiredSessions() {
        List<ConnectionManager.ExpiredSessionInfo> expired = connectionManager.getExpiredSessions();
        for (ConnectionManager.ExpiredSessionInfo session : expired) {
            // Notify the user on their private error queue
            try {
                messagingTemplate.convertAndSendToUser(
                    session.getEmail(),
                    "/queue/errors",
                    Map.of(
                        "type", "AUTH_EXPIRED",
                        "message", "Your session has expired. Please log in again."
                    )
                );
            } catch (Exception ignored) {}

            // Unregister active session
            connectionManager.unregisterSession(session.getSessionId());
        }
    }
}

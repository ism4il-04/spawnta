package com.spawnta.security;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConnectionManager {

    // email -> set of active session IDs (supports multi-tab)
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    // sessionId -> expiration time
    private final Map<String, LocalDateTime> sessionExpirations = new ConcurrentHashMap<>();

    // sessionId -> email
    private final Map<String, String> sessionUsers = new ConcurrentHashMap<>();

    // sessionId -> active chatId the user is currently viewing in this session
    private final Map<String, Long> sessionActiveChats = new ConcurrentHashMap<>();

    public void registerSession(String email, String sessionId, LocalDateTime expiration) {
        sessionUsers.put(sessionId, email);
        sessionExpirations.put(sessionId, expiration);
        userSessions.computeIfAbsent(email, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public void unregisterSession(String sessionId) {
        String email = sessionUsers.remove(sessionId);
        sessionExpirations.remove(sessionId);
        sessionActiveChats.remove(sessionId);

        if (email != null) {
            Set<String> sessions = userSessions.get(email);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(email);
                }
            }
        }
    }

    public void subscribeChat(String sessionId, Long chatId) {
        sessionActiveChats.put(sessionId, chatId);
    }

    public void unsubscribeChat(String sessionId) {
        sessionActiveChats.remove(sessionId);
    }

    public boolean isUserActiveInChat(String email, Long chatId) {
        Set<String> sessions = userSessions.get(email);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        for (String sessionId : sessions) {
            Long activeChatId = sessionActiveChats.get(sessionId);
            if (chatId.equals(activeChatId)) {
                return true;
            }
        }
        return false;
    }

    public List<ExpiredSessionInfo> getExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<ExpiredSessionInfo> expired = new ArrayList<>();
        sessionExpirations.forEach((sessionId, exp) -> {
            if (exp.isBefore(now)) {
                String email = sessionUsers.get(sessionId);
                if (email != null) {
                    expired.add(new ExpiredSessionInfo(sessionId, email));
                }
            }
        });
        return expired;
    }

    public Map<String, String> getSessionUsers() {
        return sessionUsers;
    }

    public static class ExpiredSessionInfo {
        private final String sessionId;
        private final String email;

        public ExpiredSessionInfo(String sessionId, String email) {
            this.sessionId = sessionId;
            this.email = email;
        }

        public String getSessionId() { return sessionId; }
        public String getEmail() { return email; }
    }
}

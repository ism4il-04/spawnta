package com.spawnta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spawnta.entity.OutboxEvent;
import com.spawnta.entity.OutboxEventStatus;
import com.spawnta.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class OutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);
    private static final int MAX_RETRY_COUNT = 3;

    private final OutboxEventRepository outboxEventRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public OutboxProcessor(OutboxEventRepository outboxEventRepository,
                          SimpMessagingTemplate messagingTemplate,
                          ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Traite les événements en attente toutes les 500ms pour garantir la réactivité temps réel
     */
    @Scheduled(fixedDelay = 500)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findAllByStatusOrderByIdAsc(OutboxEventStatus.PENDING);
        
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                processEvent(event);
                event.setStatus(OutboxEventStatus.SENT);
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to process outbox event ID {}: {}", event.getId(), e.getMessage());
                handleEventFailure(event);
            }
        }
    }

    private void processEvent(OutboxEvent event) throws Exception {
        Map<String, Object> payload = objectMapper.readValue(event.getPayload(), Map.class);

        switch (event.getTopic()) {
            case "chat.messages":
                processChatMessageEvent(payload);
                break;
            case "chat.participants":
                processChatParticipantEvent(payload);
                break;
            default:
                log.warn("Unknown outbox event topic: {}", event.getTopic());
        }
    }

    private void processChatMessageEvent(Map<String, Object> payload) {
        Long chatId = getLongValue(payload.get("chatId"));
        String action = (String) payload.get("action");
        Map<String, Object> message = (Map<String, Object>) payload.get("message");

        if (chatId == null || action == null || message == null) {
            log.error("Invalid chat.messages event payload: missing required fields");
            return;
        }

        String destination = "/topic/chats/" + chatId;
        
        // Construire le payload WebSocket
        Map<String, Object> wsPayload = Map.of(
            "type", "MESSAGE".equals(action) || "NEW".equals(action) ? "MESSAGE" : "MESSAGE_DELETED",
            "payload", message
        );

        log.info("Broadcasting {} to {}", action, destination);
        messagingTemplate.convertAndSend(destination, (Object) wsPayload);
    }

    private void processChatParticipantEvent(Map<String, Object> payload) {
        Long chatId = getLongValue(payload.get("chatId"));
        String action = (String) payload.get("action");

        if (chatId == null || action == null) {
            log.error("Invalid chat.participants event payload: missing required fields");
            return;
        }

        String destination = "/topic/chats/" + chatId;
        
        Map<String, Object> wsPayload = Map.of(
            "type", "PARTICIPANT_UPDATE",
            "payload", Map.of(
                "action", action,
                "userId", payload.getOrDefault("userId", null),
                "firstName", payload.getOrDefault("firstName", ""),
                "lastName", payload.getOrDefault("lastName", "")
            )
        );

        log.info("Broadcasting participant {} to {}", action, destination);
        messagingTemplate.convertAndSend(destination, (Object) wsPayload);
    }

    private void handleEventFailure(OutboxEvent event) {
        event.setRetryCount(event.getRetryCount() + 1);
        
        if (event.getRetryCount() >= MAX_RETRY_COUNT) {
            log.error("Outbox event ID {} exceeded max retry count, marking as FAILED", event.getId());
            event.setStatus(OutboxEventStatus.FAILED);
        }
        
        outboxEventRepository.save(event);
    }

    private Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Nettoie les événements complétés de plus de 7 jours (optionnel)
     */
    @Scheduled(cron = "0 0 2 * * *") // Tous les jours à 2h du matin
    @Transactional
    public void cleanupOldEvents() {
        // TODO: Implémenter le nettoyage des anciens événements si nécessaire
        log.info("Outbox cleanup task executed");
    }
}

package com.spawnta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class KafkaChatConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaChatConsumer.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Thread-safe LRU cache to store processed event IDs and ensure idempotency (Requirement 6.5)
    private final Map<String, Boolean> processedEvents = Collections.synchronizedMap(
        new LinkedHashMap<String, Boolean>(1000, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                return size() > 1000;
            }
        }
    );

    public KafkaChatConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "chat.messages", groupId = "spawnta-chat-group")
    public void consumeMessageEvent(String payload) {
        try {
            Map<String, Object> eventData = objectMapper.readValue(payload, Map.class);
            String eventId = (String) eventData.get("eventId");
            Long chatId = ((Number) eventData.get("chatId")).longValue();

            // 1. Idempotency Check
            if (eventId != null && processedEvents.containsKey(eventId)) {
                log.debug("Skipping duplicate message event ID: {}", eventId);
                return;
            }
            if (eventId != null) {
                processedEvents.put(eventId, true);
            }

            log.info("Consuming chat message event ID {} for chat room {}", eventId, chatId);

            // 2. Broadcast to STOMP WebSocket topic /topic/chats/{chatId}
            messagingTemplate.convertAndSend("/topic/chats/" + chatId, (Object) Map.of(
                "type", "MESSAGE",
                "payload", eventData.get("message")
            ));
        } catch (Exception e) {
            log.error("Error processing chat message event from Kafka: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "chat.participants", groupId = "spawnta-chat-group")
    public void consumeParticipantEvent(String payload) {
        try {
            Map<String, Object> eventData = objectMapper.readValue(payload, Map.class);
            String eventId = (String) eventData.get("eventId");
            Long chatId = ((Number) eventData.get("chatId")).longValue();

            // 1. Idempotency Check
            if (eventId != null && processedEvents.containsKey(eventId)) {
                log.debug("Skipping duplicate participant event ID: {}", eventId);
                return;
            }
            if (eventId != null) {
                processedEvents.put(eventId, true);
            }

            log.info("Consuming chat participant event ID {} for chat room {}", eventId, chatId);

            // 2. Broadcast to STOMP WebSocket topic /topic/chats/{chatId}
            messagingTemplate.convertAndSend("/topic/chats/" + chatId, (Object) Map.of(
                "type", "PARTICIPANT_UPDATE",
                "payload", eventData
            ));
        } catch (Exception e) {
            log.error("Error processing chat participant event from Kafka: {}", e.getMessage(), e);
        }
    }
}

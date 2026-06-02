package com.spawnta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spawnta.entity.Activity;
import com.spawnta.entity.NotificationType;
import com.spawnta.repository.ActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;
    private final ActivityRepository activityRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Idempotency cache to prevent duplicate events processing
    private final Map<String, Boolean> processedEvents = Collections.synchronizedMap(
        new LinkedHashMap<String, Boolean>(1000, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                return size() > 1000;
            }
        }
    );

    public NotificationConsumer(NotificationService notificationService,
                                ActivityRepository activityRepository) {
        this.notificationService = notificationService;
        this.activityRepository = activityRepository;
    }

    @KafkaListener(topics = "user.level_up", groupId = "spawnta-notifications-group")
    public void consumeLevelUpEvent(String payload) {
        try {
            Map<String, Object> eventData = objectMapper.readValue(payload, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            String eventId = (String) eventData.get("eventId");
            Long userId = ((Number) eventData.get("userId")).longValue();
            int newLevel = ((Number) eventData.get("newLevel")).intValue();

            // Idempotency Check
            if (eventId != null && processedEvents.containsKey(eventId)) {
                log.debug("Skipping duplicate level-up event ID: {}", eventId);
                return;
            }
            if (eventId != null) {
                processedEvents.put(eventId, true);
            }

            log.info("Processing Level Up notification for user ID: {} to Level: {}", userId, newLevel);

            String title = "Niveau Supérieur !";
            String message = "Félicitations ! Vous êtes passé au niveau " + newLevel + " ! Continuez comme ça !";

            notificationService.sendNotification(userId, NotificationType.PERSONAL, title, message, null, null);
        } catch (Exception e) {
            log.error("Error processing level-up event from Kafka: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "attendance.confirmed", groupId = "spawnta-notifications-group")
    public void consumeAttendanceConfirmedEvent(String payload) {
        try {
            Map<String, Object> eventData = objectMapper.readValue(payload, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            String eventId = (String) eventData.get("eventId");
            Long participantId = ((Number) eventData.get("participantId")).longValue();
            Long activityId = ((Number) eventData.get("activityId")).longValue();

            // Idempotency Check
            if (eventId != null && processedEvents.containsKey(eventId)) {
                log.debug("Skipping duplicate attendance confirmed event ID: {}", eventId);
                return;
            }
            if (eventId != null) {
                processedEvents.put(eventId, true);
            }

            log.info("Processing Attendance Confirmed notification for user ID: {}, activity ID: {}", participantId, activityId);

            Activity activity = activityRepository.findById(activityId)
                    .orElseThrow(() -> new IllegalArgumentException("Activity not found with ID: " + activityId));

            String title = "Présence Validée !";
            String message = "Votre présence à l'activité \"" + activity.getTitle() + "\" a été validée par l'organisateur. Vous avez gagné +100 XP !";

            notificationService.sendNotification(participantId, NotificationType.PERSONAL, title, message, activityId, null);
        } catch (Exception e) {
            log.error("Error processing attendance confirmed event from Kafka: {}", e.getMessage(), e);
        }
    }
}

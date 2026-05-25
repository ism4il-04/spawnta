package com.spawnta.service;

import com.spawnta.entity.Chat;
import com.spawnta.entity.ChatParticipant;
import com.spawnta.entity.ChatParticipantStatus;
import com.spawnta.entity.User;
import com.spawnta.repository.ChatParticipantRepository;
import com.spawnta.security.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int MAX_NOTIFICATIONS_PER_MINUTE = 5;

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConnectionManager connectionManager;
    private final ChatParticipantRepository participantRepository;

    public NotificationService(StringRedisTemplate redisTemplate,
                               SimpMessagingTemplate messagingTemplate,
                               ConnectionManager connectionManager,
                               ChatParticipantRepository participantRepository) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.connectionManager = connectionManager;
        this.participantRepository = participantRepository;
    }

    public void sendNewMessageNotification(Chat chat, User sender, String content) {
        Long chatId = chat.getId();
        String chatTitle = chat.getType() == com.spawnta.entity.ChatType.GROUP 
                ? chat.getActivity().getTitle() 
                : sender.getFirstName() + " " + sender.getLastName();

        String senderName = sender.getFirstName() + " " + sender.getLastName();
        String preview = content.substring(0, Math.min(content.length(), 60)) + (content.length() > 60 ? "..." : "");

        log.debug("Dispatching notifications for new message in chat ID {}", chatId);

        // Load all participants to send them notifications
        for (ChatParticipant cp : chat.getParticipants()) {
            User recipient = cp.getUser();

            // 1. Skip if sender is the recipient
            if (recipient.getId().equals(sender.getId())) {
                continue;
            }

            // 2. Respect mute/kicked status or disabled notifications preference (Requirement 7.2)
            if (cp.getStatus() != ChatParticipantStatus.ACTIVE || !cp.isNotificationsEnabled()) {
                log.debug("Skipping notification for user {} - Notifications disabled or participant not active", recipient.getEmail());
                continue;
            }

            // 3. Proactively suppress notification if the user is currently viewing this chat (Requirement 7.4)
            if (connectionManager.isUserActiveInChat(recipient.getEmail(), chatId)) {
                log.debug("Skipping notification for user {} - User is actively viewing chat ID {}", recipient.getEmail(), chatId);
                continue;
            }

            // 4. Rate-limit notifications (max 5 per minute per chat, Requirement 7.5)
            String rateLimitKey = "chat:notifications:rate:" + chatId + ":" + recipient.getId();
            Long count = redisTemplate.opsForValue().increment(rateLimitKey);
            if (count != null) {
                if (count == 1) {
                    redisTemplate.expire(rateLimitKey, 60, TimeUnit.SECONDS);
                }
                if (count > MAX_NOTIFICATIONS_PER_MINUTE) {
                    log.debug("Skipping notification for user {} due to rate-limiting in chat ID {}", recipient.getEmail(), chatId);
                    continue;
                }
            }

            // 5. Send real-time notification using STOMP secure user destination (Requirement 7.1 & 7.3)
            try {
                messagingTemplate.convertAndSendToUser(
                    recipient.getEmail(),
                    "/queue/notifications",
                    Map.of(
                        "chatId", chatId,
                        "chatTitle", chatTitle,
                        "senderName", senderName,
                        "preview", preview
                    )
                );
                log.debug("Successfully sent message notification to user {}", recipient.getEmail());
            } catch (Exception e) {
                log.error("Failed to send notification to user {}: {}", recipient.getEmail(), e.getMessage());
            }
        }
    }
}

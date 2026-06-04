package com.spawnta.service;

import com.spawnta.entity.*;
import com.spawnta.repository.*;
import com.spawnta.security.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    
    private final UserNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    public NotificationService(StringRedisTemplate redisTemplate,
                               SimpMessagingTemplate messagingTemplate,
                               ConnectionManager connectionManager,
                               ChatParticipantRepository participantRepository,
                               UserNotificationRepository notificationRepository,
                               UserRepository userRepository,
                               ActivityRepository activityRepository) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.connectionManager = connectionManager;
        this.participantRepository = participantRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
    }

    /**
     * Dispatch new message notifications over STOMP WebSocket secure queues.
     */
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

    /**
     * Send real-time gamification / system alert notifications to users.
     */
    @Transactional
    public UserNotification sendNotification(Long userId, NotificationType type, String title, String message,
                                             Long relatedActivityId, Long relatedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        log.info("Sending notification of type {} to user {}: {}", type, user.getEmail(), title);

        UserNotification notification = new UserNotification(user, type, title, message);

        if (relatedActivityId != null) {
            activityRepository.findById(relatedActivityId).ifPresent(notification::setRelatedActivity);
        }

        if (relatedUserId != null) {
            userRepository.findById(relatedUserId).ifPresent(notification::setRelatedUser);
        }

        notification = notificationRepository.save(notification);

        // Send over STOMP WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + userId, (Object) Map.of(
                    "id", notification.getId(),
                    "type", type.name(),
                    "title", title,
                    "message", message != null ? message : "",
                    "isRead", false,
                    "createdAt", notification.getCreatedAt().toString()
            ));
        } catch (Exception e) {
            log.error("Failed to broadcast notification over WebSocket to user ID: {}", userId, e);
        }

        return notification;
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        UserNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + notificationId));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional(readOnly = true)
    public List<UserNotification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
}

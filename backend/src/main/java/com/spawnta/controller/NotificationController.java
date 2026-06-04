package com.spawnta.controller;

import com.spawnta.entity.User;
import com.spawnta.entity.UserNotification;
import com.spawnta.repository.UserRepository;
import com.spawnta.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getNotifications(@AuthenticationPrincipal String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<UserNotification> notifications = notificationService.getUserNotifications(user.getId());

        List<Map<String, Object>> response = notifications.stream()
                .map(n -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", n.getId());
                    map.put("type", n.getType().name());
                    map.put("title", n.getTitle());
                    map.put("message", n.getMessage() != null ? n.getMessage() : "");
                    map.put("isRead", n.isRead());
                    map.put("createdAt", n.getCreatedAt().toString());
                    map.put("relatedActivityId", n.getRelatedActivity() != null ? n.getRelatedActivity().getId() : "");
                    map.put("relatedUserId", n.getRelatedUser() != null ? n.getRelatedUser().getId() : "");
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read."));
    }
}

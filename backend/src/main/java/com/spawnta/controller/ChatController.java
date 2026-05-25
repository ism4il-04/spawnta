package com.spawnta.controller;

import com.spawnta.dto.ChatResponse;
import com.spawnta.dto.CreatePrivateChatRequest;
import com.spawnta.dto.MessageResponse;
import com.spawnta.entity.Chat;
import com.spawnta.entity.Message;
import com.spawnta.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<List<ChatResponse>> getUserChats(Authentication authentication) {
        String email = authentication.getName();
        List<ChatResponse> chats = chatService.getUserChats(email);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<MessageResponse>> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        String email = authentication.getName();
        Page<MessageResponse> messages = chatService.getChatMessages(chatId, email, PageRequest.of(page, size));
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/private")
    public ResponseEntity<ChatResponse> createPrivateChat(
            @Valid @RequestBody CreatePrivateChatRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        Chat chat = chatService.createPrivateChat(email, request.targetUserId());
        
        // Load the chat response details
        List<ChatResponse> userChats = chatService.getUserChats(email);
        ChatResponse matched = userChats.stream()
                .filter(c -> c.id().equals(chat.getId()))
                .findFirst()
                .orElse(new ChatResponse(
                    chat.getId(), "PRIVATE", null, null, "ACTIVE", chat.getCreatedAt(),
                    "Conversation", null, "", null, null, true
                ));

        return ResponseEntity.status(HttpStatus.CREATED).body(matched);
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long chatId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String email = authentication.getName();
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu du message ne peut pas être vide");
        }

        Message msg = chatService.sendMessage(chatId, email, content);
        MessageResponse response = new MessageResponse(
            msg.getId(),
            msg.getChat().getId(),
            msg.getSender() != null ? msg.getSender().getId() : null,
            msg.getSender() != null ? (msg.getSender().getFirstName() + " " + msg.getSender().getLastName()) : "Utilisateur Supprimé",
            msg.getSender() != null ? msg.getSender().getAvatarUrl() : null,
            msg.getContent(),
            msg.getStatus().name(),
            msg.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{chatId}/messages/{messageId}")
    public ResponseEntity<Map<String, String>> deleteMessage(
            @PathVariable Long chatId,
            @PathVariable Long messageId,
            Authentication authentication) {
        String email = authentication.getName();
        chatService.deleteMessage(messageId, email);
        return ResponseEntity.ok(Map.of("message", "Message supprimé avec succès"));
    }

    @PutMapping("/{chatId}/participants/{userId}/mute")
    public ResponseEntity<Map<String, String>> muteParticipant(
            @PathVariable Long chatId,
            @PathVariable Long userId,
            Authentication authentication) {
        String email = authentication.getName();
        chatService.muteParticipant(chatId, userId, email);
        return ResponseEntity.ok(Map.of("message", "Participant rendu muet avec succès"));
    }

    @PutMapping("/{chatId}/participants/{userId}/kick")
    public ResponseEntity<Map<String, String>> kickParticipant(
            @PathVariable Long chatId,
            @PathVariable Long userId,
            Authentication authentication) {
        String email = authentication.getName();
        chatService.kickParticipant(chatId, userId, email);
        return ResponseEntity.ok(Map.of("message", "Participant exclu du salon avec succès"));
    }

    @PutMapping("/{chatId}/notifications")
    public ResponseEntity<Map<String, String>> toggleNotifications(
            @PathVariable Long chatId,
            @RequestBody Map<String, Boolean> body,
            Authentication authentication) {
        String email = authentication.getName();
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            throw new IllegalArgumentException("Le champ 'enabled' est obligatoire");
        }
        chatService.toggleNotifications(chatId, email, enabled);
        return ResponseEntity.ok(Map.of("message", "Préférence de notifications mise à jour"));
    }

    @DeleteMapping("/{chatId}/leave")
    public ResponseEntity<Map<String, String>> leaveChat(
            @PathVariable Long chatId,
            Authentication authentication) {
        String email = authentication.getName();
        chatService.leaveChat(chatId, email);
        return ResponseEntity.ok(Map.of("message", "Vous avez quitté la conversation"));
    }

    @PostMapping("/{chatId}/block")
    public ResponseEntity<Map<String, String>> blockPrivateChat(
            @PathVariable Long chatId,
            Authentication authentication) {
        String email = authentication.getName();
        chatService.blockPrivateChat(chatId, email);
        return ResponseEntity.ok(Map.of("message", "La conversation privée a été bloquée"));
    }

    @PostMapping("/{chatId}/unblock")
    public ResponseEntity<Map<String, String>> unblockPrivateChat(
            @PathVariable Long chatId,
            Authentication authentication) {
        String email = authentication.getName();
        chatService.unblockPrivateChat(chatId, email);
        return ResponseEntity.ok(Map.of("message", "La conversation privée a été débloquée"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}

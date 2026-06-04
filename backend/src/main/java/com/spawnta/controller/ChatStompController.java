package com.spawnta.controller;

import com.spawnta.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class ChatStompController {

    private final ChatService chatService;

    public ChatStompController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chats/{chatId}/send")
    public void receiveStompMessage(
            @DestinationVariable Long chatId,
            @Payload Map<String, String> payload,
            Principal principal) {
        if (principal == null) {
            return;
        }
        
        String email = principal.getName();
        String content = payload.get("content");
        
        if (content != null && !content.trim().isEmpty()) {
            try {
                chatService.sendMessage(chatId, email, content);
            } catch (Exception ignored) {
                // Log and absorb validation or mute/kick constraints errors on websocket
            }
        }
    }
}

package com.spawnta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spawnta.entity.*;
import com.spawnta.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MessageRepository messageRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ChatAuditLogRepository chatAuditLogRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final MessageParser messageParser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ChatRepository chatRepository,
                       ChatParticipantRepository chatParticipantRepository,
                       MessageRepository messageRepository,
                       OutboxEventRepository outboxEventRepository,
                       ChatAuditLogRepository chatAuditLogRepository,
                       UserRepository userRepository,
                       ActivityRepository activityRepository,
                       MessageParser messageParser) {
        this.chatRepository = chatRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.messageRepository = messageRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.chatAuditLogRepository = chatAuditLogRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.messageParser = messageParser;
    }

    @Transactional
    public Chat createGroupChat(Activity activity) {
        log.info("Creating automatically a group chat for activity ID: {}", activity.getId());
        Chat chat = new Chat(ChatType.GROUP, activity);
        chat = chatRepository.save(chat);

        // Add the host of the activity as an ACTIVE chat participant
        ChatParticipant hostParticipant = new ChatParticipant(chat, activity.getHost());
        hostParticipant.setStatus(ChatParticipantStatus.ACTIVE);
        chatParticipantRepository.save(hostParticipant);

        return chat;
    }

    @Transactional
    public void addParticipantToGroupChat(Long activityId, Long userId) {
        Chat chat = chatRepository.findByActivityId(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Group chat not found for activity ID " + activityId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found ID " + userId));

        Optional<ChatParticipant> existing = chatParticipantRepository.findByChatIdAndUserId(chat.getId(), userId);
        if (existing.isPresent()) {
            ChatParticipant participant = existing.get();
            if (participant.getStatus() != ChatParticipantStatus.ACTIVE) {
                participant.setStatus(ChatParticipantStatus.ACTIVE);
                chatParticipantRepository.save(participant);
            } else {
                return; // Already active
            }
        } else {
            ChatParticipant newParticipant = new ChatParticipant(chat, user);
            newParticipant.setStatus(ChatParticipantStatus.ACTIVE);
            chatParticipantRepository.save(newParticipant);
        }

        // Write "JOIN" outbox event
        createParticipantOutboxEvent(chat.getId(), user, "JOIN");
    }

    @Transactional
    public void removeParticipantFromGroupChat(Long activityId, Long userId) {
        Chat chat = chatRepository.findByActivityId(activityId).orElse(null);
        if (chat == null) {
            return;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        chatParticipantRepository.findByChatIdAndUserId(chat.getId(), userId).ifPresent(cp -> {
            chatParticipantRepository.delete(cp);
            createParticipantOutboxEvent(chat.getId(), user, "LEAVE");
        });
    }

    @Transactional
    public Chat createPrivateChat(Long user1Id, Long user2Id) {
        if (user1Id.equals(user2Id)) {
            throw new IllegalArgumentException("You cannot create a private chat with yourself");
        }

        // Check if private chat already exists
        Optional<Chat> existing = chatRepository.findPrivateChatBetweenUsers(user1Id, user2Id);
        if (existing.isPresent()) {
            return existing.get();
        }

        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new IllegalArgumentException("User 1 not found"));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new IllegalArgumentException("User 2 not found"));

        log.info("Creating a new private chat between users {} and {}", user1Id, user2Id);
        Chat chat = new Chat(ChatType.PRIVATE, null);
        chat = chatRepository.save(chat);

        ChatParticipant p1 = new ChatParticipant(chat, user1);
        p1.setStatus(ChatParticipantStatus.ACTIVE);
        chatParticipantRepository.save(p1);

        ChatParticipant p2 = new ChatParticipant(chat, user2);
        p2.setStatus(ChatParticipantStatus.ACTIVE);
        chatParticipantRepository.save(p2);

        createParticipantOutboxEvent(chat.getId(), user1, "JOIN");
        createParticipantOutboxEvent(chat.getId(), user2, "JOIN");

        return chat;
    }

    @Transactional
    public Message sendMessage(Long chatId, Long senderId, String rawContent) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        if (chat.getStatus() == ChatStatus.BLOCKED) {
            throw new IllegalStateException("This chat is blocked.");
        }

        ChatParticipant participant = chatParticipantRepository.findByChatIdAndUserId(chatId, senderId)
                .orElseThrow(() -> new IllegalStateException("You are not a participant in this chat."));

        if (participant.getStatus() == ChatParticipantStatus.MUTED) {
            throw new IllegalStateException("You have been muted by a moderator in this chat.");
        }
        if (participant.getStatus() == ChatParticipantStatus.KICKED) {
            throw new IllegalStateException("You have been kicked from this chat.");
        }

        // Sanitize and escape message content
        String sanitizedContent = messageParser.parseAndSanitize(rawContent);

        Message message = new Message(chat, participant.getUser(), sanitizedContent);
        message = messageRepository.save(message);

        // Save transactional outbox event
        createMessageOutboxEvent(message, "NEW");

        return message;
    }

    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        Chat chat = message.getChat();
        boolean isAuthor = message.getSender() != null && message.getSender().getId().equals(userId);
        boolean isModerator = chat.getType() == ChatType.GROUP && chat.getActivity().getHost().getId().equals(userId);

        if (!isAuthor && !isModerator) {
            throw new IllegalStateException("You are not authorized to delete this message.");
        }

        message.setStatus(MessageStatus.DELETED);
        messageRepository.save(message);

        if (isModerator && !isAuthor) {
            // Log moderation action
            User moderator = userRepository.findById(userId).orElseThrow();
            ChatAuditLog audit = new ChatAuditLog(
                moderator, 
                chat, 
                "DELETE_MESSAGE", 
                message.getSender(), 
                "Deleted message ID: " + messageId
            );
            chatAuditLogRepository.save(audit);
        }

        // Save transactional outbox event for deletion
        createMessageOutboxEvent(message, "DELETE");
    }

    @Transactional
    public void muteParticipant(Long chatId, Long targetUserId, Long moderatorId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        if (chat.getType() != ChatType.GROUP) {
            throw new IllegalArgumentException("Moderation is only available for group chats.");
        }

        if (!chat.getActivity().getHost().getId().equals(moderatorId)) {
            throw new IllegalStateException("Only the host can moderate the chat.");
        }

        ChatParticipant participant = chatParticipantRepository.findByChatIdAndUserId(chatId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("The targeted participant does not exist in this chat."));

        participant.setStatus(ChatParticipantStatus.MUTED);
        chatParticipantRepository.save(participant);

        User moderator = userRepository.findById(moderatorId).orElseThrow();
        ChatAuditLog audit = new ChatAuditLog(moderator, chat, "MUTE", participant.getUser(), "Muted");
        chatAuditLogRepository.save(audit);

        createParticipantOutboxEvent(chatId, participant.getUser(), "MUTE");
    }

    @Transactional
    public void kickParticipant(Long chatId, Long targetUserId, Long moderatorId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        if (chat.getType() != ChatType.GROUP) {
            throw new IllegalArgumentException("Moderation is only available for group chats.");
        }

        if (!chat.getActivity().getHost().getId().equals(moderatorId)) {
            throw new IllegalStateException("Only the host can moderate the chat.");
        }

        ChatParticipant participant = chatParticipantRepository.findByChatIdAndUserId(chatId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("The targeted participant does not exist in this chat."));

        participant.setStatus(ChatParticipantStatus.KICKED);
        chatParticipantRepository.save(participant);

        User moderator = userRepository.findById(moderatorId).orElseThrow();
        ChatAuditLog audit = new ChatAuditLog(moderator, chat, "KICK", participant.getUser(), "Kicked from chat");
        chatAuditLogRepository.save(audit);

        createParticipantOutboxEvent(chatId, participant.getUser(), "KICK");
    }

    @Transactional
    public void leaveChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        if (chat.getType() == ChatType.PRIVATE) {
            throw new IllegalStateException("You cannot leave a private chat.");
        }

        ChatParticipant participant = chatParticipantRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new IllegalArgumentException("You are not a participant in this chat."));

        participant.setStatus(ChatParticipantStatus.LEFT);
        chatParticipantRepository.save(participant);

        User user = userRepository.findById(userId).orElseThrow();
        createParticipantOutboxEvent(chatId, user, "LEAVE");
    }

    @Transactional
    public void deleteConversation(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        ChatParticipant participant = chatParticipantRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new IllegalArgumentException("You are not a participant in this chat."));

        participant.setStatus(ChatParticipantStatus.DELETED);
        chatParticipantRepository.save(participant);
    }

    @Transactional
    public void deleteChatByActivityId(Long activityId) {
        chatRepository.findByActivityId(activityId).ifPresent(chat -> {
            log.info("Deleting group chat for activity ID: {}", activityId);
            chatRepository.delete(chat);
        });
    }

    @Transactional
    public void blockPrivateChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        if (chat.getType() != ChatType.PRIVATE) {
            throw new IllegalArgumentException("Only private chats can be blocked.");
        }

        boolean participates = chatParticipantRepository.existsByChatIdAndUserId(chatId, userId);
        if (!participates) {
            throw new IllegalStateException("You are not a participant in this chat.");
        }

        chat.setStatus(ChatStatus.BLOCKED);
        chat.setBlockedByUserId(userId);
        chatRepository.save(chat);
    }

    @Transactional
    public void unblockPrivateChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        if (chat.getType() != ChatType.PRIVATE) {
            throw new IllegalArgumentException("Only private chats can be unblocked.");
        }

        boolean participates = chatParticipantRepository.existsByChatIdAndUserId(chatId, userId);
        if (!participates) {
            throw new IllegalStateException("You are not a participant in this chat.");
        }

        if (chat.getBlockedByUserId() != null && !chat.getBlockedByUserId().equals(userId)) {
            throw new IllegalStateException("Only the user who blocked the conversation can unblock it.");
        }

        chat.setStatus(ChatStatus.ACTIVE);
        chat.setBlockedByUserId(null);
        chatRepository.save(chat);
    }

    @Transactional
    public void toggleNotifications(Long chatId, Long userId, boolean enabled) {
        ChatParticipant participant = chatParticipantRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        participant.setNotificationsEnabled(enabled);
        chatParticipantRepository.save(participant);
    }

    // ─── Outbox Helpers ──────────────────────────────────────────────────────

    private void createMessageOutboxEvent(Message message, String action) {
        try {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", message.getId());
            messageMap.put("chatId", message.getChat().getId());
            messageMap.put("content", message.getContent());
            messageMap.put("status", message.getStatus().name());
            messageMap.put("createdAt", message.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            if (message.getSender() != null) {
                messageMap.put("senderId", message.getSender().getId());
                messageMap.put("senderFirstName", message.getSender().getFirstName());
                messageMap.put("senderLastName", message.getSender().getLastName());
                messageMap.put("senderAvatarUrl", message.getSender().getAvatarUrl());
            } else {
                messageMap.put("senderId", null);
                messageMap.put("senderFirstName", "User");
                messageMap.put("senderLastName", "Deleted");
                messageMap.put("senderAvatarUrl", null);
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", UUID.randomUUID().toString());
            payload.put("chatId", message.getChat().getId());
            payload.put("action", action); // NEW, DELETE
            payload.put("message", messageMap);

            String jsonPayload = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = new OutboxEvent("chat.messages", jsonPayload);
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Failed to serialize message event to outbox", e);
            throw new RuntimeException("Failed to register outbox event", e);
        }
    }

    private void createParticipantOutboxEvent(Long chatId, User user, String action) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", UUID.randomUUID().toString());
            payload.put("chatId", chatId);
            payload.put("userId", user.getId());
            payload.put("userEmail", user.getEmail());
            payload.put("firstName", user.getFirstName());
            payload.put("lastName", user.getLastName());
            payload.put("action", action); // JOIN, LEAVE, MUTE, KICK
            payload.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            String jsonPayload = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = new OutboxEvent("chat.participants", jsonPayload);
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Failed to serialize participant event to outbox", e);
        }
    }

    // ─── Controller Integration Helpers ──────────────────────────────────────

    @Transactional(readOnly = true)
    public List<com.spawnta.dto.ChatResponse> getUserChats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Chat> chats = chatRepository.findAllActiveChatsByUserId(user.getId());
        List<com.spawnta.dto.ChatResponse> responses = new ArrayList<>();

        for (Chat chat : chats) {
            try {
                String title = "";
                String avatarUrl = null;

                if (chat.getType() == ChatType.GROUP) {
                    title = (chat.getActivity() != null) ? chat.getActivity().getTitle() : "Cancelled Activity";
                } else {
                    // Find other participant safely
                    User other = null;
                    if (chat.getParticipants() != null) {
                        other = chat.getParticipants().stream()
                                .filter(cp -> cp.getUser() != null)
                                .map(ChatParticipant::getUser)
                                .filter(u -> !u.getId().equals(user.getId()))
                                .findFirst()
                                .orElse(null);
                    }

                    if (other != null) {
                        title = (other.getFirstName() != null ? other.getFirstName() : "") + " " + 
                                (other.getLastName() != null ? other.getLastName() : "");
                        if (title.trim().isEmpty()) title = "User " + other.getId();
                        avatarUrl = other.getAvatarUrl();
                    } else {
                        title = "Deleted User";
                    }
                }

                // Get last message safely
                String lastMessage = "";
                LocalDateTime lastMessageTime = null;
                String lastMessageSender = null;

                org.springframework.data.domain.Page<Message> lastMsgs = messageRepository
                        .findAllByChatIdAndStatusOrderByCreatedAtDesc(chat.getId(), MessageStatus.ACTIVE, org.springframework.data.domain.PageRequest.of(0, 1));
                
                if (lastMsgs != null && lastMsgs.hasContent()) {
                    Message msg = lastMsgs.getContent().get(0);
                    lastMessage = msg.getContent() != null ? msg.getContent() : "";
                    lastMessageTime = msg.getCreatedAt();
                    if (msg.getSender() != null) {
                        lastMessageSender = (msg.getSender().getFirstName() != null ? msg.getSender().getFirstName() : "") + " " + 
                                           (msg.getSender().getLastName() != null ? msg.getSender().getLastName() : "");
                        if (lastMessageSender.trim().isEmpty()) lastMessageSender = "User " + msg.getSender().getId();
                    } else {
                        lastMessageSender = "Deleted User";
                    }
                }

                boolean notificationsEnabled = true;
                String participantStatus = "ACTIVE";

                if (chat.getParticipants() != null) {
                    ChatParticipant self = chat.getParticipants().stream()
                            .filter(cp -> cp.getUser() != null && cp.getUser().getId().equals(user.getId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (self != null) {
                        notificationsEnabled = self.isNotificationsEnabled();
                        participantStatus = self.getStatus() != null ? self.getStatus().name() : "ACTIVE";
                    }
                }

                responses.add(new com.spawnta.dto.ChatResponse(
                    chat.getId(),
                    chat.getType() != null ? chat.getType().name() : "GROUP",
                    chat.getActivity() != null ? chat.getActivity().getId() : null,
                    chat.getActivity() != null ? chat.getActivity().getTitle() : null,
                    chat.getStatus() != null ? chat.getStatus().name() : "ACTIVE",
                    chat.getCreatedAt() != null ? chat.getCreatedAt() : LocalDateTime.now(),
                    title,
                    avatarUrl,
                    lastMessage,
                    lastMessageTime,
                    lastMessageSender,
                    notificationsEnabled,
                    chat.getBlockedByUserId(),
                    participantStatus
                ));
            } catch (Exception e) {
                log.error("Error mapping chat ID {} to response for user {}", chat.getId(), email, e);
                // Skip this individual chat instead of failing the whole request
            }
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.spawnta.dto.MessageResponse> getChatMessages(Long chatId, String email, org.springframework.data.domain.Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ChatParticipant participant = chatParticipantRepository.findByChatIdAndUserId(chatId, user.getId())
                .orElseThrow(() -> new IllegalStateException("You are not authorized to access this conversation."));

        Chat chat = participant.getChat();

        org.springframework.data.domain.Page<Message> messages;

        // For GROUP chats: only show messages since the user joined (Req: privacy on join)
        if (chat.getType() == ChatType.GROUP) {
            messages = messageRepository.findAllByChatIdAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(
                chatId, MessageStatus.ACTIVE, participant.getJoinedAt(), pageable
            );
        } else {
            // For PRIVATE chats: show all messages
            messages = messageRepository.findAllByChatIdAndStatusOrderByCreatedAtDesc(chatId, MessageStatus.ACTIVE, pageable);
        }

        return messages.map(msg -> new com.spawnta.dto.MessageResponse(
            msg.getId(),
            msg.getChat().getId(),
            msg.getSender() != null ? msg.getSender().getId() : null,
            msg.getSender() != null ? (msg.getSender().getFirstName() + " " + msg.getSender().getLastName()) : "Deleted User",
            msg.getSender() != null ? msg.getSender().getAvatarUrl() : null,
            msg.getContent(),
            msg.getStatus().name(),
            msg.getCreatedAt()
        ));
    }

    @Transactional
    public Chat createPrivateChat(String email, Long targetUserId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return createPrivateChat(user.getId(), targetUserId);
    }

    @Transactional
    public Message sendMessage(Long chatId, String email, String rawContent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return sendMessage(chatId, user.getId(), rawContent);
    }

    @Transactional
    public void deleteMessage(Long messageId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        deleteMessage(messageId, user.getId());
    }

    @Transactional
    public void muteParticipant(Long chatId, Long targetUserId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        muteParticipant(chatId, targetUserId, user.getId());
    }

    @Transactional
    public void kickParticipant(Long chatId, Long targetUserId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        kickParticipant(chatId, targetUserId, user.getId());
    }

    @Transactional
    public void leaveChat(Long chatId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        leaveChat(chatId, user.getId());
    }

    @Transactional
    public void deleteConversation(Long chatId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        deleteConversation(chatId, user.getId());
    }

    @Transactional
    public void blockPrivateChat(Long chatId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        blockPrivateChat(chatId, user.getId());
    }

    @Transactional
    public void unblockPrivateChat(Long chatId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        unblockPrivateChat(chatId, user.getId());
    }

    @Transactional
    public void toggleNotifications(Long chatId, String email, boolean enabled) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        toggleNotifications(chatId, user.getId(), enabled);
    }
}

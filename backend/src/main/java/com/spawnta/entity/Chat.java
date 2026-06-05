package com.spawnta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chats")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private Activity activity; // Nullable for PRIVATE chats

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatStatus status = ChatStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "blocked_by_user_id")
    private Long blockedByUserId;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Constructors ────────────────────────────────────
    public Chat() {}

    public Chat(ChatType type, Activity activity) {
        this.type = type;
        this.activity = activity;
    }

    // ─── Getters / Setters ───────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ChatType getType() { return type; }
    public void setType(ChatType type) { this.type = type; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public ChatStatus getStatus() { return status; }
    public void setStatus(ChatStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getBlockedByUserId() { return blockedByUserId; }
    public void setBlockedByUserId(Long blockedByUserId) { this.blockedByUserId = blockedByUserId; }

    public List<ChatParticipant> getParticipants() { return participants; }
    public void setParticipants(List<ChatParticipant> participants) { this.participants = participants; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
}

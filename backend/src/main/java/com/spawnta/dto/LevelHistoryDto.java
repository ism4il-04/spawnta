package com.spawnta.dto;

import java.time.LocalDateTime;

public class LevelHistoryDto {
    private Long id;
    private int oldLevel;
    private int newLevel;
    private LocalDateTime achievedAt;

    public LevelHistoryDto() {}

    public LevelHistoryDto(Long id, int oldLevel, int newLevel, LocalDateTime achievedAt) {
        this.id = id;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.achievedAt = achievedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getOldLevel() { return oldLevel; }
    public void setOldLevel(int oldLevel) { this.oldLevel = oldLevel; }

    public int getNewLevel() { return newLevel; }
    public void setNewLevel(int newLevel) { this.newLevel = newLevel; }

    public LocalDateTime getAchievedAt() { return achievedAt; }
    public void setAchievedAt(LocalDateTime achievedAt) { this.achievedAt = achievedAt; }
}

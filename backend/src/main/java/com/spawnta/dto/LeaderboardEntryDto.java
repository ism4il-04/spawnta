package com.spawnta.dto;

public class LeaderboardEntryDto {
    private Long userId;
    private String name;
    private String avatarUrl;
    private int level;
    private int totalXpEarned;

    public LeaderboardEntryDto() {}

    public LeaderboardEntryDto(Long userId, String name, String avatarUrl, int level, int totalXpEarned) {
        this.userId = userId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.level = level;
        this.totalXpEarned = totalXpEarned;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getTotalXpEarned() { return totalXpEarned; }
    public void setTotalXpEarned(int totalXpEarned) { this.totalXpEarned = totalXpEarned; }
}

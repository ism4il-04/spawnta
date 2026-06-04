package com.spawnta.dto;

import java.util.List;

public class GamificationProfileDto {
    private int level;
    private int xp;
    private int currentLevelXpRequired;
    private int totalXpEarned;
    private List<BadgeDto> achievements;
    private List<LevelHistoryDto> history;

    public GamificationProfileDto() {}

    public GamificationProfileDto(int level, int xp, int currentLevelXpRequired, int totalXpEarned,
                                  List<BadgeDto> achievements, List<LevelHistoryDto> history) {
        this.level = level;
        this.xp = xp;
        this.currentLevelXpRequired = currentLevelXpRequired;
        this.totalXpEarned = totalXpEarned;
        this.achievements = achievements;
        this.history = history;
    }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getCurrentLevelXpRequired() { return currentLevelXpRequired; }
    public void setCurrentLevelXpRequired(int currentLevelXpRequired) { this.currentLevelXpRequired = currentLevelXpRequired; }

    public int getTotalXpEarned() { return totalXpEarned; }
    public void setTotalXpEarned(int totalXpEarned) { this.totalXpEarned = totalXpEarned; }

    public List<BadgeDto> getAchievements() { return achievements; }
    public void setAchievements(List<BadgeDto> achievements) { this.achievements = achievements; }

    public List<LevelHistoryDto> getHistory() { return history; }
    public void setHistory(List<LevelHistoryDto> history) { this.history = history; }
}

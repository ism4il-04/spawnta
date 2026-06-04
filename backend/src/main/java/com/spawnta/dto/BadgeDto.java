package com.spawnta.dto;

public class BadgeDto {
    private Integer id;
    private String name;
    private String description;
    private String iconUrl;
    private Integer xpReward;

    public BadgeDto() {}

    public BadgeDto(Integer id, String name, String description, String iconUrl, Integer xpReward) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.xpReward = xpReward;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public Integer getXpReward() { return xpReward; }
    public void setXpReward(Integer xpReward) { this.xpReward = xpReward; }
}

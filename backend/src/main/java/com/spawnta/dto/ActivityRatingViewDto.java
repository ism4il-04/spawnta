package com.spawnta.dto;

public class ActivityRatingViewDto {
    private Long id;
    private Integer ratingScore;
    private String comment;
    private String raterName;
    private String raterAvatarUrl;
    private String createdAt;

    public ActivityRatingViewDto() {}

    public ActivityRatingViewDto(Long id, Integer ratingScore, String comment,
                                 String raterName, String raterAvatarUrl, String createdAt) {
        this.id = id;
        this.ratingScore = ratingScore;
        this.comment = comment;
        this.raterName = raterName;
        this.raterAvatarUrl = raterAvatarUrl;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Integer getRatingScore() { return ratingScore; }
    public String getComment() { return comment; }
    public String getRaterName() { return raterName; }
    public String getRaterAvatarUrl() { return raterAvatarUrl; }
    public String getCreatedAt() { return createdAt; }
}

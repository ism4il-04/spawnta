package com.spawnta.dto;

public class ActivityRatingDto {
    private Long activityId;
    private Integer ratingScore;
    private String comment;

    public ActivityRatingDto() {}

    public ActivityRatingDto(Long activityId, Integer ratingScore, String comment) {
        this.activityId = activityId;
        this.ratingScore = ratingScore;
        this.comment = comment;
    }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public Integer getRatingScore() { return ratingScore; }
    public void setRatingScore(Integer ratingScore) { this.ratingScore = ratingScore; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

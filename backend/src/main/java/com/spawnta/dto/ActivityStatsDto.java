package com.spawnta.dto;

public class ActivityStatsDto {
    private Double averageRating;
    private Long reviewCount;

    public ActivityStatsDto() {}

    public ActivityStatsDto(Double averageRating, Long reviewCount) {
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getReviewCount() { return reviewCount; }
    public void setReviewCount(Long reviewCount) { this.reviewCount = reviewCount; }
}

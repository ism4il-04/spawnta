package com.spawnta.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RateActivityRequest {
    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimale est 1")
    @Max(value = 5, message = "La note maximale est 5")
    private Integer ratingScore;

    private String comment;

    public RateActivityRequest() {}

    public RateActivityRequest(Integer ratingScore, String comment) {
        this.ratingScore = ratingScore;
        this.comment = comment;
    }

    public Integer getRatingScore() { return ratingScore; }
    public void setRatingScore(Integer ratingScore) { this.ratingScore = ratingScore; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

package com.spawnta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Current user's relationship to an activity (attendance / rating eligibility).
 */
public class ActivityParticipationStatusDto {
    private boolean host;
    private boolean joined;
    @JsonProperty("pendingRequest")
    private boolean pendingRequest;
    @JsonProperty("canCheckIn")
    private boolean canCheckIn;
    @JsonProperty("canRate")
    private boolean canRate;
    @JsonProperty("hasRated")
    private boolean hasRated;
    private String attendanceStatus;

    public ActivityParticipationStatusDto() {}

    public ActivityParticipationStatusDto(boolean host, boolean joined, boolean pendingRequest,
                                          boolean canCheckIn, boolean canRate, boolean hasRated,
                                          String attendanceStatus) {
        this.host = host;
        this.joined = joined;
        this.pendingRequest = pendingRequest;
        this.canCheckIn = canCheckIn;
        this.canRate = canRate;
        this.hasRated = hasRated;
        this.attendanceStatus = attendanceStatus;
    }

    public boolean isHost() { return host; }
    public boolean isJoined() { return joined; }
    public boolean isPendingRequest() { return pendingRequest; }
    public boolean isCanCheckIn() { return canCheckIn; }
    public boolean isCanRate() { return canRate; }
    public boolean isHasRated() { return hasRated; }
    public String getAttendanceStatus() { return attendanceStatus; }
}

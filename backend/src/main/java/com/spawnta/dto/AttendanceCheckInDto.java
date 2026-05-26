package com.spawnta.dto;

import java.time.LocalDateTime;

public class AttendanceCheckInDto {
    private Long attendanceId;
    private String qrCode;
    private String activityName;
    private LocalDateTime checkInDeadline;

    public AttendanceCheckInDto() {}

    public AttendanceCheckInDto(Long attendanceId, String qrCode, String activityName, LocalDateTime checkInDeadline) {
        this.attendanceId = attendanceId;
        this.qrCode = qrCode;
        this.activityName = activityName;
        this.checkInDeadline = checkInDeadline;
    }

    public Long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public LocalDateTime getCheckInDeadline() { return checkInDeadline; }
    public void setCheckInDeadline(LocalDateTime checkInDeadline) { this.checkInDeadline = checkInDeadline; }
}

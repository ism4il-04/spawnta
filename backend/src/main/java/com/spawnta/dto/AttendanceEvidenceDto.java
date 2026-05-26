package com.spawnta.dto;

public class AttendanceEvidenceDto {
    private String photoUrl;
    private Double latitude;
    private Double longitude;

    public AttendanceEvidenceDto() {}

    public AttendanceEvidenceDto(String photoUrl, Double latitude, Double longitude) {
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}

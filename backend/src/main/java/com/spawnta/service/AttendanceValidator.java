package com.spawnta.service;

import com.spawnta.entity.Activity;
import com.spawnta.entity.ActivityAttendance;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AttendanceValidator {

    private static final Logger log = LoggerFactory.getLogger(AttendanceValidator.class);
    private static final double EARTH_RADIUS_METERS = 6371000;
    private static final double ALLOWED_CHECKIN_RADIUS_METERS = 200.0; // 200 meters check-in radius
    private static final int BEFORE_START_WINDOW_MINUTES = 15;
    private static final int AFTER_END_WINDOW_MINUTES = 30;

    /**
     * Validates that the check-in location is within the allowed radius (200m) of the activity location.
     */
    public boolean validateGeolocation(Point activityLoc, Point checkinLoc) {
        if (activityLoc == null || checkinLoc == null) {
            log.warn("Cannot validate location: activity or check-in location is null");
            return false;
        }

        double distance = calculateDistanceInMeters(
                activityLoc.getY(), activityLoc.getX(), // y is Latitude, x is Longitude in typical EPSG:4326 Point
                checkinLoc.getY(), checkinLoc.getX()
        );

        log.debug("Calculated check-in distance: {} meters. Allowed: {} meters", distance, ALLOWED_CHECKIN_RADIUS_METERS);
        return distance <= ALLOWED_CHECKIN_RADIUS_METERS;
    }

    /**
     * Checks if the check-in time is within the allowed window:
     * [scheduledAt - 15 mins, scheduledAt + duration + 30 mins]
     */
    public boolean validateTimeWindow(Activity activity, LocalDateTime checkinTime) {
        LocalDateTime scheduledStart = activity.getScheduledAt();
        int duration = activity.getDurationMinutes() != null ? activity.getDurationMinutes() : 120; // default 2 hours

        LocalDateTime windowStart = scheduledStart.minusMinutes(BEFORE_START_WINDOW_MINUTES);
        LocalDateTime windowEnd = scheduledStart.plusMinutes(duration).plusMinutes(AFTER_END_WINDOW_MINUTES);

        boolean withinWindow = !checkinTime.isBefore(windowStart) && !checkinTime.isAfter(windowEnd);

        log.debug("Check-in time: {}. Window: [{} to {}]. Valid: {}", checkinTime, windowStart, windowEnd, withinWindow);
        return withinWindow;
    }

    /**
     * Extract or parse photo metadata (stubs for photo evidence validation).
     */
    public boolean validatePhotoEvidence(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            log.warn("Photo evidence is empty or null");
            return false;
        }
        // In real-world, we could verify image format, content using Rekognition/Vision, or metadata.
        // For spawnta backend, simple check for non-empty url is sufficient.
        return true;
    }

    /**
     * Haversine formula to compute great-circle distance between two GPS coordinates.
     */
    private double calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }
}

package com.spawnta.repository;

import com.spawnta.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /**
     * Find activities within a given radius (in meters) of a point.
     * Uses PostGIS ST_DWithin on the geography cast for accurate distance in meters.
     */
    @Query(value = """
        SELECT a.* FROM activities a
        WHERE ST_DWithin(
            a.location::geography,
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
            :radiusMeters
        )
        AND a.location IS NOT NULL
        AND a.scheduled_at > now()
        AND (:category IS NULL OR a.category = :category)
        AND (:participationMode IS NULL OR a.participation_mode = :participationMode)
        AND (:activityType IS NULL OR a.activity_type = :activityType)
        AND (:scheduledFrom IS NULL OR a.scheduled_at >= :scheduledFrom)
        AND (:scheduledTo IS NULL OR a.scheduled_at <= :scheduledTo)
        ORDER BY a.scheduled_at ASC
        """, nativeQuery = true)
    List<Activity> findNearbyMeetups(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radiusMeters") double radiusMeters,
        @Param("category") String category,
        @Param("participationMode") String participationMode,
        @Param("activityType") String activityType,
        @Param("scheduledFrom") LocalDateTime scheduledFrom,
        @Param("scheduledTo") LocalDateTime scheduledTo
    );

    /**
     * Find trip activities where the start_location is within radius.
     */
    @Query(value = """
        SELECT a.* FROM activities a
        WHERE ST_DWithin(
            a.start_location::geography,
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
            :radiusMeters
        )
        AND a.start_location IS NOT NULL
        AND a.scheduled_at > now()
        AND (:category IS NULL OR a.category = :category)
        AND (:participationMode IS NULL OR a.participation_mode = :participationMode)
        AND (:activityType IS NULL OR a.activity_type = :activityType)
        AND (:scheduledFrom IS NULL OR a.scheduled_at >= :scheduledFrom)
        AND (:scheduledTo IS NULL OR a.scheduled_at <= :scheduledTo)
        ORDER BY a.scheduled_at ASC
        """, nativeQuery = true)
    List<Activity> findNearbyTrips(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radiusMeters") double radiusMeters,
        @Param("category") String category,
        @Param("participationMode") String participationMode,
        @Param("activityType") String activityType,
        @Param("scheduledFrom") LocalDateTime scheduledFrom,
        @Param("scheduledTo") LocalDateTime scheduledTo
    );

    /**
     * Count activities created by a host after a given date (for weekly limit check).
     */
    long countByHostIdAndCreatedAtAfter(Long hostId, LocalDateTime after);
}

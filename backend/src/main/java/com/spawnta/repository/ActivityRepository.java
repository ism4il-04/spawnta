package com.spawnta.repository;

import com.spawnta.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        AND (CAST(:category AS text) IS NULL OR a.category = CAST(:category AS text))
        AND (CAST(:participationMode AS text) IS NULL OR a.participation_mode = CAST(:participationMode AS text))
        AND (CAST(:activityType AS text) IS NULL OR a.activity_type = CAST(:activityType AS text))
        AND (CAST(:scheduledFrom AS timestamp) IS NULL OR a.scheduled_at >= CAST(:scheduledFrom AS timestamp))
        AND (CAST(:scheduledTo AS timestamp) IS NULL OR a.scheduled_at <= CAST(:scheduledTo AS timestamp))
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
        AND (CAST(:category AS text) IS NULL OR a.category = CAST(:category AS text))
        AND (CAST(:participationMode AS text) IS NULL OR a.participation_mode = CAST(:participationMode AS text))
        AND (CAST(:activityType AS text) IS NULL OR a.activity_type = CAST(:activityType AS text))
        AND (CAST(:scheduledFrom AS timestamp) IS NULL OR a.scheduled_at >= CAST(:scheduledFrom AS timestamp))
        AND (CAST(:scheduledTo AS timestamp) IS NULL OR a.scheduled_at <= CAST(:scheduledTo AS timestamp))
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

    long countByHostId(Long hostId);

    List<Activity> findByHostId(Long hostId);

    @Query("SELECT DISTINCT a FROM Activity a LEFT JOIN FETCH a.participants WHERE a.host.id = :hostId")
    List<Activity> findByHostIdWithParticipants(Long hostId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Activity a WHERE a.id = :id")
    Optional<Activity> findByIdWithLock(@Param("id") Long id);

    List<Activity> findAllByScheduledAtAfter(LocalDateTime time);
}

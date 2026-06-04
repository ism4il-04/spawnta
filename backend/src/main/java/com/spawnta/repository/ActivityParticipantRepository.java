package com.spawnta.repository;

import com.spawnta.entity.ActivityParticipant;
import com.spawnta.entity.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Long> {

    Optional<ActivityParticipant> findByActivityIdAndUserId(Long activityId, Long userId);

    boolean existsByActivityIdAndUserId(Long activityId, Long userId);

    long countByActivityIdAndStatus(Long activityId, ParticipationStatus status);

    List<ActivityParticipant> findByUserId(Long userId);

    List<ActivityParticipant> findAllByActivityIdAndStatusOrderByJoinedAtAsc(Long activityId, ParticipationStatus status);
}

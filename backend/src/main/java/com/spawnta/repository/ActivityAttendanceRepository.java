package com.spawnta.repository;

import com.spawnta.entity.ActivityAttendance;
import com.spawnta.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ActivityAttendanceRepository extends JpaRepository<ActivityAttendance, Long> {
    Optional<ActivityAttendance> findByActivityIdAndParticipantId(Long activityId, Long participantId);
    List<ActivityAttendance> findByParticipantId(Long participantId);
    List<ActivityAttendance> findByActivityId(Long activityId);
    long countByParticipantIdAndStatus(Long participantId, AttendanceStatus status);
}

package com.spawnta.repository;

import com.spawnta.entity.AttendanceEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceEvidenceRepository extends JpaRepository<AttendanceEvidence, Long> {
    List<AttendanceEvidence> findByAttendanceId(Long attendanceId);
}

package com.spawnta.moderation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spawnta.moderation.entity.ActivityReport;
import com.spawnta.moderation.entity.ReportStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityReportRepository extends JpaRepository<ActivityReport, Long> {
    List<ActivityReport> findByStatus(ReportStatus status);
    List<ActivityReport> findByActivityId(Integer activityId);
    List<ActivityReport> findByReportedById(Long reportedById);
    List<ActivityReport> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Modifying
    @Query("DELETE FROM ActivityReport ar WHERE ar.activity.id = :activityId")
    void deleteByActivityId(@Param("activityId") Long activityId);
    
    @Query("SELECT ar FROM ActivityReport ar WHERE ar.status = :status ORDER BY ar.createdAt DESC")
    List<ActivityReport> findOpenReports(@Param("status") ReportStatus status);
    
    Long countByStatus(ReportStatus status);
}

package com.spawnta.moderation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spawnta.moderation.entity.UserReport;
import com.spawnta.moderation.entity.ReportStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    List<UserReport> findByStatus(ReportStatus status);
    List<UserReport> findByReportedUserId(Long reportedUserId);
    List<UserReport> findByReportedById(Long reportedById);
    List<UserReport> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT ur FROM UserReport ur WHERE ur.status = :status ORDER BY ur.createdAt DESC")
    List<UserReport> findOpenReports(@Param("status") ReportStatus status);
    
    Long countByStatus(ReportStatus status);
}

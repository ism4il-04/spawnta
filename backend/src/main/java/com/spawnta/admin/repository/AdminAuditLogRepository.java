package com.spawnta.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spawnta.admin.entity.AdminAuditLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {
    List<AdminAuditLog> findByAdminId(Long adminId);
    List<AdminAuditLog> findByAction(String action);
    List<AdminAuditLog> findByTargetType(String targetType);
    List<AdminAuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<AdminAuditLog> findByAdminIdAndCreatedAtBetween(Long adminId, LocalDateTime startDate, LocalDateTime endDate);
}

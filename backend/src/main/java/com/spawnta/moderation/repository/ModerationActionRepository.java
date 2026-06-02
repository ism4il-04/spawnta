package com.spawnta.moderation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spawnta.moderation.entity.ModerationAction;
import com.spawnta.moderation.entity.ActionType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ModerationActionRepository extends JpaRepository<ModerationAction, Long> {
    List<ModerationAction> findByActionType(ActionType actionType);
    List<ModerationAction> findByTargetId(Integer targetId);
    List<ModerationAction> findByInitiatedById(Long initiatedById);
    List<ModerationAction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<ModerationAction> findByInitiatedByIdAndCreatedAtBetween(Long initiatedById, LocalDateTime startDate, LocalDateTime endDate);
}

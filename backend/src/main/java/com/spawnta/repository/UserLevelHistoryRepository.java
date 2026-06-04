package com.spawnta.repository;

import com.spawnta.entity.UserLevelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserLevelHistoryRepository extends JpaRepository<UserLevelHistory, Long> {
    List<UserLevelHistory> findByUserIdOrderByAchievedAtDesc(Long userId);
}

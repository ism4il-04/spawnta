package com.spawnta.repository;

import com.spawnta.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Integer> {
    Optional<Badge> findByName(String name);
}

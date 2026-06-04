package com.spawnta.repository;

import com.spawnta.entity.OutboxEvent;
import com.spawnta.entity.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findAllByStatusOrderByIdAsc(OutboxEventStatus status);
}

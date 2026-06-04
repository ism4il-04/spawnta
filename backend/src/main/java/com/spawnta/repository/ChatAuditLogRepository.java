package com.spawnta.repository;

import com.spawnta.entity.ChatAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatAuditLogRepository extends JpaRepository<ChatAuditLog, Long> {

    List<ChatAuditLog> findAllByChatIdOrderByCreatedAtDesc(Long chatId);
}

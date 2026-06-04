package com.spawnta.repository;

import com.spawnta.entity.Message;
import com.spawnta.entity.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findAllByChatIdAndStatusOrderByCreatedAtDesc(Long chatId, MessageStatus status, Pageable pageable);

    // Only messages sent after the user joined (for group chat members)
    Page<Message> findAllByChatIdAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(
        Long chatId, MessageStatus status, LocalDateTime after, Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM Message m " +
           "WHERE m.chat.id IN (SELECT c.id FROM Chat c WHERE c.type = com.spawnta.entity.ChatType.GROUP) " +
           "AND m.createdAt < :cutoff")
    int deleteExpiredGroupMessages(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("DELETE FROM Message m " +
           "WHERE m.chat.id IN (SELECT c.id FROM Chat c WHERE c.type = com.spawnta.entity.ChatType.PRIVATE) " +
           "AND m.createdAt < :cutoff")
    int deleteExpiredPrivateMessages(@Param("cutoff") LocalDateTime cutoff);
}

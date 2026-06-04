package com.spawnta.repository;

import com.spawnta.entity.ChatParticipant;
import com.spawnta.entity.ChatParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    Optional<ChatParticipant> findByChatIdAndUserId(Long chatId, Long userId);

    boolean existsByChatIdAndUserId(Long chatId, Long userId);

    List<ChatParticipant> findAllByChatId(Long chatId);

    List<ChatParticipant> findAllByChatIdAndStatus(Long chatId, ChatParticipantStatus status);
}

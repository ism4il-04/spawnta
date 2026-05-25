package com.spawnta.repository;

import com.spawnta.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByActivityId(Long activityId);

    @Query("SELECT cp.chat FROM ChatParticipant cp WHERE cp.user.id = :userId AND cp.status = com.spawnta.entity.ChatParticipantStatus.ACTIVE ORDER BY cp.chat.id DESC")
    List<Chat> findAllActiveChatsByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Chat c " +
           "JOIN c.participants cp1 " +
           "JOIN c.participants cp2 " +
           "WHERE c.type = com.spawnta.entity.ChatType.PRIVATE " +
           "AND cp1.user.id = :user1Id " +
           "AND cp2.user.id = :user2Id")
    Optional<Chat> findPrivateChatBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}

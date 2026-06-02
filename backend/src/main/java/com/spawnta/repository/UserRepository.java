package com.spawnta.repository;

import com.spawnta.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByStripeCustomerId(String stripeCustomerId);

    List<User> findTop50ByOrderByLevelDescTotalXpEarnedDesc();

    @Query("SELECT u FROM User u WHERE u.isBanned = false AND u.suspendedUntil IS NOT NULL AND u.suspendedUntil <= :now")
    List<User> findExpiredSuspensions(@Param("now") LocalDateTime now);
}

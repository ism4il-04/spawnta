package com.spawnta.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spawnta.subscription.entity.PaymentTransaction;
import com.spawnta.subscription.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    List<PaymentTransaction> findByUserId(Long userId);
    List<PaymentTransaction> findByStatus(PaymentStatus status);
    List<PaymentTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<PaymentTransaction> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}

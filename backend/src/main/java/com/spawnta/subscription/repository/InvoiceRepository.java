package com.spawnta.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spawnta.subscription.entity.Invoice;
import com.spawnta.subscription.entity.InvoiceStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByStripeInvoiceId(String stripeInvoiceId);
    
    List<Invoice> findByUserId(Long userId);
    List<Invoice> findByStatus(InvoiceStatus status);
    List<Invoice> findByInvoiceDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Invoice> findByUserIdAndInvoiceDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}

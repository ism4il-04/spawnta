package com.spawnta.service;

import com.spawnta.entity.OutboxEvent;
import com.spawnta.entity.OutboxEventStatus;
import com.spawnta.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OutboxPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherScheduler.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisherScheduler(OutboxEventRepository outboxEventRepository,
                                    KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 2000) // Runs every 2 seconds
    @Transactional
    public void publishPendingEvents() {
        // Fetch all pending events ordered by id (strict order)
        List<OutboxEvent> pendingEvents = outboxEventRepository.findAllByStatusOrderByIdAsc(OutboxEventStatus.PENDING);
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                // Publish to Kafka synchronously to guarantee ordering and handle exceptions immediately
                kafkaTemplate.send(event.getTopic(), event.getPayload())
                        .get(3, TimeUnit.SECONDS);

                // Success
                event.setStatus(OutboxEventStatus.SENT);
                outboxEventRepository.save(event);
                log.debug("Successfully published outbox event ID {} to topic {}", event.getId(), event.getTopic());
            } catch (Exception e) {
                // Kafka broker is down or timed out.
                log.error("Failed to publish outbox event ID {} to topic {}. Broker may be unavailable. Error: {}", 
                        event.getId(), event.getTopic(), e.getMessage());

                // Update event state to FAILED and increment retry count
                event.setStatus(OutboxEventStatus.FAILED);
                event.setRetryCount(event.getRetryCount() + 1);
                outboxEventRepository.save(event);

                // IMPORTANT: Stop the loop immediately!
                // If we continue, we might publish subsequent events, breaking the strict temporal ordering of chat messages.
                // Stopping the loop allows the next scheduler run to retry this failed event first once Kafka recovers.
                return;
            }
        }
    }

    @Scheduled(fixedRate = 10000) // Every 10 seconds, retry failed events by resetting their status to PENDING
    @Transactional
    public void retryFailedEvents() {
        List<OutboxEvent> failedEvents = outboxEventRepository.findAllByStatusOrderByIdAsc(OutboxEventStatus.FAILED);
        if (!failedEvents.isEmpty()) {
            log.info("Retrying {} failed outbox events", failedEvents.size());
            for (OutboxEvent event : failedEvents) {
                event.setStatus(OutboxEventStatus.PENDING);
                outboxEventRepository.save(event);
            }
        }
    }
}

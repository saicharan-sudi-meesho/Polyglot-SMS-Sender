package com.polyglot.sms.sender.repository;

import com.polyglot.sms.sender.entity.FailedKafkaEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.repository.query.Param;
@Repository
public interface FailedEventRepository extends JpaRepository<FailedKafkaEvent, Long> {
    @Query(value = """
        UPDATE failed_kafka_events
        SET status = 'PROCESSING',
            processing_at = :now
        WHERE id IN (
            SELECT id FROM failed_kafka_events
            WHERE status = 'PENDING'
            OR (status = 'PROCESSING' AND processing_at < :timeoutThreshold)
            ORDER BY created_at ASC
            LIMIT 50
            FOR UPDATE SKIP LOCKED
        ) 
        RETURNING *
        """, nativeQuery = true)
    List<FailedKafkaEvent> claimExpiredOrPending(@Param("now") long now, @Param("timeoutThreshold") long timeoutThreshold);
}

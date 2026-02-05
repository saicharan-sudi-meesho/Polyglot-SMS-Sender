package com.polyglot.sms.sender.repository;

import com.polyglot.sms.sender.entity.FailedKafkaEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FailedEventRepository extends JpaRepository<FailedKafkaEvent, Long> {
    @Query(value = """
        UPDATE failed_kafka_events
        SET status = 'PROCESSING'
        WHERE id IN (
            SELECT id FROM failed_kafka_events
            WHERE status = 'PENDING'
            ORDER BY created_at ASC
            LIMIT 50
            FOR UPDATE SKIP LOCKED
        ) 
        RETURNING *
        """, nativeQuery = true)
    List<FailedKafkaEvent> claimAndGetTop50Pending();
}

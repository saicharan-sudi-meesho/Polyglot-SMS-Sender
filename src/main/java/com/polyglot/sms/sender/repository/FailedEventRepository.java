package com.polyglot.sms.sender.repository;

import com.polyglot.sms.sender.entity.FailedKafkaEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FailedEventRepository extends JpaRepository<FailedKafkaEvent, Long> {
    @Query(value = "SELECT * FROM failed_kafka_events ORDER BY created_at ASC LIMIT 50", nativeQuery = true)
    List<FailedKafkaEvent> findTop50Oldest();
}

package com.polyglot.sms.sender.entity;

import lombok.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;


@Entity
@Table(name = "failed_kafka_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedKafkaEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String topic;
    private String eventKey;
    
    @Column(columnDefinition = "TEXT") // Stores the JSON payload
    private String eventPayload;
    
    private long createdAt;
    @Builder.Default
    private String status = "PENDING"; // PENDING, PROCESSING
    @Builder.Default
    private int retryCount = 0;
}

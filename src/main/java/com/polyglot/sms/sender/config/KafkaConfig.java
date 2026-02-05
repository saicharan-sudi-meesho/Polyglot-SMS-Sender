package com.polyglot.sms.sender.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;


@Configuration
public class KafkaConfig {
    
    @Value("${app.kafka.topic.sms-events}")
    private String topicName;

    // Add these injected values
    @Value("${app.kafka.topic.partitions}")
    private int partitions;

    @Value("${app.kafka.topic.replication-factor}")
    private short replicationFactor;

    @Value("${app.kafka.topic.min-insync-replicas:2}")
    private String minInsyncReplicas;

    @Bean
    public NewTopic smsEventsTopic() {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("min.insync.replicas", minInsyncReplicas)
                .build();
    }
}

package com.polyglot.sms.sender.service;

import com.polyglot.sms.sender.entity.FailedKafkaEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import com.polyglot.sms.sender.dto.SmsEvent;
import com.polyglot.sms.sender.dto.SmsStatus;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.polyglot.sms.sender.repository.FailedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private FailedEventRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private final String topic = "test-topic";
    private final String key = "user-123";

    private final SmsEvent payload = SmsEvent.builder()
            .userId("user-123")
            .messageContent("hello")
            .status(SmsStatus.SUCCESS)
            .idempotencyKey("unique-id-123")
            .build();

    @Test
    @DisplayName("Should successfully send message to Kafka")
    void testSendMessage_Success() {
        // Arrange
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        SendResult<String, Object> sendResult = (SendResult<String, Object>) mock(SendResult.class);
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(topic, 0), 0L, 0, 0L, 0, 0);
        
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        future.complete(sendResult);
        when(kafkaTemplate.send(topic, key, payload)).thenReturn(future);

        // Act
        kafkaProducerService.sendMessage(topic, key, payload);

        // Assert
        verify(kafkaTemplate).send(topic, key, payload);
        verify(repository, never()).save(any(FailedKafkaEvent.class));
    }

    @Test
    @DisplayName("Should save to SQL when Kafka throws Synchronous Exception")
    void testSendMessage_SyncFailure() throws Exception{
        // Arrange
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Kafka Down"));
        when(objectMapper.writeValueAsString(payload)).thenReturn("{\"json\":\"payload\"}");

        // Act
        kafkaProducerService.sendMessage(topic, key, payload);

        // Assert
        verify(repository, times(1)).save(any(FailedKafkaEvent.class));
        
        // Verify correct data saved to fallback
        ArgumentCaptor<FailedKafkaEvent> captor = ArgumentCaptor.forClass(FailedKafkaEvent.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEventKey()).isEqualTo(key);
        assertThat(captor.getValue().getTopic()).isEqualTo(topic);
        assertThat(captor.getValue().getIdempotencyKey()).isEqualTo(payload.getIdempotencyKey());
    }

    @Test
    @DisplayName("Should save to SQL when Kafka fails Asynchronously")
    void testSendMessage_AsyncFailure() throws Exception{
        // Arrange: send() works, but the future fails later
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Network issue mid-send"));
        
        when(kafkaTemplate.send(topic, key, payload)).thenReturn(future);
        when(objectMapper.writeValueAsString(payload)).thenReturn("{\"json\":\"payload\"}");

        // Act
        kafkaProducerService.sendMessage(topic, key, payload);

        // Assert
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(repository, times(1)).save(any(FailedKafkaEvent.class));
        });
    }

    @Test
    @DisplayName("Should log error when both Kafka and SQL fail")
    void testSendMessage_TotalFailure() throws Exception {
        // Arrange
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Kafka Dead"));
        when(objectMapper.writeValueAsString(payload)).thenReturn("{\"json\":\"payload\"}");
        when(repository.save(any(FailedKafkaEvent.class)))
                .thenThrow(new RuntimeException("SQL Dead"));

        // Act & Assert
        kafkaProducerService.sendMessage(topic, key, payload);
        
        verify(kafkaTemplate).send(topic, key, payload);
        verify(repository).save(any(FailedKafkaEvent.class));
    }

    @Test
    @DisplayName("Should handle JSON exception gracefully")
    void testSendMessage_JsonFailure() throws Exception {
        // Arrange
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Kafka Down"));
        
        // Simulate Jackson failing
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(mock(com.fasterxml.jackson.core.JsonProcessingException.class));

        // Act
        kafkaProducerService.sendMessage(topic, key, payload);

        // Assert
        verify(repository, never()).save(any()); // Should not save if JSON fails
    }
}
package com.example.steamlensbackend.kafka.producer;

import com.example.steamlensbackend.config.properties.AsyncRequestProperties;
import com.example.steamlensbackend.kafka.dto.SharedLibraryRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedLibraryRequestProducer {

    private final KafkaTemplate<String, SharedLibraryRequestMessage> kafkaTemplate;
    private final AsyncRequestProperties asyncRequestProperties;

    public void sendRequest(SharedLibraryRequestMessage message) {
        CompletableFuture<SendResult<String, SharedLibraryRequestMessage>> future =
                kafkaTemplate.send(asyncRequestProperties.kafka().topic(), message.requestId(), message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message for requestId={} to topic={} partition={} offset={}",
                        message.requestId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send message for requestId={}: {}",
                        message.requestId(), ex.getMessage(), ex);
            }
        });
    }
}

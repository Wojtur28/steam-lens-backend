package com.example.steamlensbackend.kafka.producer;

import com.example.steamlensbackend.kafka.dto.AchievementRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementRequestProducer {

    private final KafkaTemplate<String, AchievementRequestMessage> achievementKafkaTemplate;

    @Value("${async.request.kafka.achievement-topic:achievement-requests}")
    private String topicName;

    public void sendRequest(AchievementRequestMessage message) {
        CompletableFuture<SendResult<String, AchievementRequestMessage>> future =
                achievementKafkaTemplate.send(topicName, message.requestId(), message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent achievement message for requestId={} to topic={} partition={} offset={}",
                        message.requestId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send achievement message for requestId={}: {}",
                        message.requestId(), ex.getMessage(), ex);
            }
        });
    }
}

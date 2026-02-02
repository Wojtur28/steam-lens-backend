package com.example.steamlensbackend.kafka.consumer;

import com.example.steamlensbackend.achievement.dto.TopAchievementsResponse;
import com.example.steamlensbackend.achievement.service.AchievementAsyncService;
import com.example.steamlensbackend.achievement.service.AchievementService;
import com.example.steamlensbackend.async.model.RequestStatus;
import com.example.steamlensbackend.async.service.AsyncRequestService;
import com.example.steamlensbackend.kafka.dto.AchievementRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementRequestConsumer {

    private final AchievementService achievementService;
    private final AchievementAsyncService achievementAsyncService;
    private final AsyncRequestService asyncRequestService;

    @KafkaListener(
            topics = "${async.request.kafka.achievement-topic:achievement-requests}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "achievementKafkaListenerContainerFactory"
    )
    public void consumeAchievementRequest(AchievementRequestMessage message) {
        String requestId = message.requestId();
        log.info("Received achievement request: requestId={}, steamId={}",
                requestId, message.steamId());

        try {
            asyncRequestService.updateStatus(requestId, RequestStatus.PROCESSING);

            TopAchievementsResponse result = achievementService.getTopAchievements(
                    message.steamId(),
                    message.apiKey(),
                    message.maxAchievements(),
                    message.language()
            );

            achievementAsyncService.saveResult(requestId, result);
            log.info("Successfully processed achievement request: requestId={}", requestId);

        } catch (Exception e) {
            log.error("Error processing achievement request: requestId={}, error={}",
                    requestId, e.getMessage(), e);
            asyncRequestService.markFailed(requestId, e.getMessage());
        }
    }
}

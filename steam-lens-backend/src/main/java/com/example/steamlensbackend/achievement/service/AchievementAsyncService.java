package com.example.steamlensbackend.achievement.service;

import com.example.steamlensbackend.achievement.dto.AchievementAsyncStatusResponse;
import com.example.steamlensbackend.achievement.dto.TopAchievementsResponse;
import com.example.steamlensbackend.async.exception.AsyncRequestNotFoundException;
import com.example.steamlensbackend.async.model.AsyncRequestState;
import com.example.steamlensbackend.async.service.AsyncRequestService;
import com.example.steamlensbackend.config.properties.AsyncRequestProperties;
import com.example.steamlensbackend.kafka.dto.AchievementRequestMessage;
import com.example.steamlensbackend.kafka.producer.AchievementRequestProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementAsyncService {

    private final AsyncRequestService asyncRequestService;
    private final AchievementRequestProducer achievementRequestProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AsyncRequestProperties asyncRequestProperties;

    private static final String RESULT_SUFFIX = ":result";

    public String initiateTopAchievementsRequest(
            String steamId,
            String apiKey,
            int maxAchievements,
            String language
    ) {
        String requestId = asyncRequestService.createRequest();

        AchievementRequestMessage message = new AchievementRequestMessage(
                requestId,
                steamId,
                apiKey,
                maxAchievements,
                language
        );

        achievementRequestProducer.sendRequest(message);

        log.info("Initiated top achievements request: requestId={}, steamId={}", requestId, steamId);
        return requestId;
    }

    public void saveResult(String requestId, TopAchievementsResponse result) {
        String resultKey = asyncRequestProperties.redis().keyPrefix() + requestId + RESULT_SUFFIX;
        redisTemplate.opsForValue().set(resultKey, result, Duration.ofHours(asyncRequestProperties.redis().ttlHours()));
        log.info("Saved achievement result for request {}", requestId);
    }

    public Optional<TopAchievementsResponse> getResult(String requestId) {
        String resultKey = asyncRequestProperties.redis().keyPrefix() + requestId + RESULT_SUFFIX;
        Object value = redisTemplate.opsForValue().get(resultKey);
        if (value != null) {
            return Optional.of(objectMapper.convertValue(value, TopAchievementsResponse.class));
        }
        return Optional.empty();
    }

    public AchievementAsyncStatusResponse getRequestStatus(String requestId) {
        AsyncRequestState state = asyncRequestService.getState(requestId)
                .orElseThrow(() -> new AsyncRequestNotFoundException(requestId));

        return switch (state.status()) {
            case PENDING -> AchievementAsyncStatusResponse.pending(
                    requestId, state.createdAt(), state.updatedAt());
            case PROCESSING -> AchievementAsyncStatusResponse.processing(
                    requestId, state.createdAt(), state.updatedAt());
            case COMPLETED -> {
                Optional<TopAchievementsResponse> resultOpt = getResult(requestId);
                yield AchievementAsyncStatusResponse.completed(
                        requestId,
                        resultOpt.orElse(null),
                        state.createdAt(),
                        state.updatedAt()
                );
            }
            case FAILED -> AchievementAsyncStatusResponse.failed(
                    requestId, state.errorMessage(), state.createdAt(), state.updatedAt());
        };
    }
}

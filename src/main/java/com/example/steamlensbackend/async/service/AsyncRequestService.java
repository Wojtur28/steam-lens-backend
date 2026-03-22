package com.example.steamlensbackend.async.service;

import com.example.steamlensbackend.async.dto.AsyncRequestStatusResponse;
import com.example.steamlensbackend.async.exception.AsyncRequestNotFoundException;
import com.example.steamlensbackend.async.model.AsyncRequestState;
import com.example.steamlensbackend.async.model.RequestStatus;
import com.example.steamlensbackend.config.properties.AsyncRequestProperties;
import com.example.steamlensbackend.family.dto.SharedLibraryPriceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncRequestService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AsyncRequestProperties asyncRequestProperties;

    private static final String STATUS_SUFFIX = ":status";
    private static final String RESULT_SUFFIX = ":result";

    public String createRequest() {
        String requestId = UUID.randomUUID().toString();
        AsyncRequestState state = AsyncRequestState.pending(requestId);
        saveState(requestId, state);
        log.info("Created async request with ID: {}", requestId);
        return requestId;
    }

    public void updateStatus(String requestId, RequestStatus status) {
        Optional<AsyncRequestState> currentState = getState(requestId);
        if (currentState.isPresent()) {
            AsyncRequestState newState = currentState.get().withStatus(status);
            saveState(requestId, newState);
            log.info("Updated request {} status to {}", requestId, status);
        }
    }

    public void markFailed(String requestId, String errorMessage) {
        Optional<AsyncRequestState> currentState = getState(requestId);
        if (currentState.isPresent()) {
            AsyncRequestState newState = currentState.get().withError(errorMessage);
            saveState(requestId, newState);
            log.error("Request {} failed: {}", requestId, errorMessage);
        }
    }

    public void saveResult(String requestId, SharedLibraryPriceResponse result) {
        String resultKey = asyncRequestProperties.redis().keyPrefix() + requestId + RESULT_SUFFIX;
        redisTemplate.opsForValue().set(resultKey, result, Duration.ofHours(asyncRequestProperties.redis().ttlHours()));
        updateStatus(requestId, RequestStatus.COMPLETED);
        log.info("Saved result for request {}", requestId);
    }

    public Optional<AsyncRequestState> getState(String requestId) {
        String statusKey = asyncRequestProperties.redis().keyPrefix() + requestId + STATUS_SUFFIX;
        Object value = redisTemplate.opsForValue().get(statusKey);
        if (value != null) {
            return Optional.of(objectMapper.convertValue(value, AsyncRequestState.class));
        }
        return Optional.empty();
    }

    public Optional<SharedLibraryPriceResponse> getResult(String requestId) {
        String resultKey = asyncRequestProperties.redis().keyPrefix() + requestId + RESULT_SUFFIX;
        Object value = redisTemplate.opsForValue().get(resultKey);
        if (value != null) {
            return Optional.of(objectMapper.convertValue(value, SharedLibraryPriceResponse.class));
        }
        return Optional.empty();
    }

    public AsyncRequestStatusResponse getRequestStatus(String requestId) {
        AsyncRequestState state = getState(requestId)
                .orElseThrow(() -> new AsyncRequestNotFoundException(requestId));

        return switch (state.status()) {
            case PENDING -> AsyncRequestStatusResponse.pending(requestId, state.createdAt(), state.updatedAt());
            case PROCESSING -> AsyncRequestStatusResponse.processing(requestId, state.createdAt(), state.updatedAt());
            case COMPLETED -> {
                Optional<SharedLibraryPriceResponse> resultOpt = getResult(requestId);
                yield AsyncRequestStatusResponse.completed(
                        requestId,
                        resultOpt.orElse(null),
                        state.createdAt(),
                        state.updatedAt()
                );
            }
            case FAILED -> AsyncRequestStatusResponse.failed(requestId, state.errorMessage(), state.createdAt(), state.updatedAt());
        };
    }

    private void saveState(String requestId, AsyncRequestState state) {
        String statusKey = asyncRequestProperties.redis().keyPrefix() + requestId + STATUS_SUFFIX;
        redisTemplate.opsForValue().set(statusKey, state, Duration.ofHours(asyncRequestProperties.redis().ttlHours()));
    }
}

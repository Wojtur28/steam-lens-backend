package com.example.steamlensbackend.achievement.dto;

import com.example.steamlensbackend.async.model.RequestStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AchievementAsyncStatusResponse(
        String requestId,
        RequestStatus status,
        TopAchievementsResponse result,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {
    public static AchievementAsyncStatusResponse pending(String requestId, Instant createdAt, Instant updatedAt) {
        return new AchievementAsyncStatusResponse(requestId, RequestStatus.PENDING, null, null, createdAt, updatedAt);
    }

    public static AchievementAsyncStatusResponse processing(String requestId, Instant createdAt, Instant updatedAt) {
        return new AchievementAsyncStatusResponse(requestId, RequestStatus.PROCESSING, null, null, createdAt, updatedAt);
    }

    public static AchievementAsyncStatusResponse completed(String requestId, TopAchievementsResponse result, Instant createdAt, Instant updatedAt) {
        return new AchievementAsyncStatusResponse(requestId, RequestStatus.COMPLETED, result, null, createdAt, updatedAt);
    }

    public static AchievementAsyncStatusResponse failed(String requestId, String error, Instant createdAt, Instant updatedAt) {
        return new AchievementAsyncStatusResponse(requestId, RequestStatus.FAILED, null, error, createdAt, updatedAt);
    }
}

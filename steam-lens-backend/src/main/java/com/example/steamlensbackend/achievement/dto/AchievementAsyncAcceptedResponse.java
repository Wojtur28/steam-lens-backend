package com.example.steamlensbackend.achievement.dto;

public record AchievementAsyncAcceptedResponse(
        String requestId,
        String message,
        String statusUrl
) {
    public static AchievementAsyncAcceptedResponse of(String requestId, String baseUrl) {
        return new AchievementAsyncAcceptedResponse(
                requestId,
                "Request accepted for processing",
                baseUrl + "/api/v1/achievements/status/" + requestId
        );
    }
}

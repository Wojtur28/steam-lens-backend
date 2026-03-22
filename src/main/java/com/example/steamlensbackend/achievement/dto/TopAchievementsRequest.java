package com.example.steamlensbackend.achievement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TopAchievementsRequest(
        @NotBlank(message = "Steam ID is required")
        String steamId,

        @Min(value = 1, message = "Max achievements must be at least 1")
        @Max(value = 100, message = "Max achievements cannot exceed 100")
        Integer maxAchievements,

        String language
) {
    public TopAchievementsRequest {
        if (maxAchievements == null) {
            maxAchievements = 20;
        }
        if (language == null || language.isBlank()) {
            language = "en";
        }
    }
}

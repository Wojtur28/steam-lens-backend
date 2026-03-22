package com.example.steamlensbackend.achievement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TopAchievementsResponse(
        String steamId,
        int totalAchievements,
        List<AchievementDetail> achievements,
        Instant fetchedAt
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AchievementDetail(
            long appId,
            String gameName,
            String achievementName,
            String achievementApiName,
            String description,
            String iconUrl,
            String iconGrayUrl,
            Instant unlockTime,
            float globalPercent,
            String rarity
    ) {}
}

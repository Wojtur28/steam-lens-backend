package com.example.steamlensbackend.kafka.dto;

import java.io.Serializable;

public record AchievementRequestMessage(
        String requestId,
        String steamId,
        String apiKey,
        int maxAchievements,
        String language
) implements Serializable {
}

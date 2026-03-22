package com.example.steamlensbackend.achievement.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SteamGlobalAchievementPercentagesResponse(
        @JsonProperty("achievementpercentages") AchievementPercentages achievementpercentages
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AchievementPercentages(
            @JsonProperty("achievements") List<Achievement> achievements
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Achievement(
            @JsonProperty("name") String name,
            @JsonProperty("percent") float percent
    ) {}
}

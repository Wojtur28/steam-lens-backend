package com.example.steamlensbackend.achievement.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SteamAchievementSchemaResponse(
        @JsonProperty("game") Game game
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Game(
            @JsonProperty("gameName") String gameName,
            @JsonProperty("gameVersion") String gameVersion,
            @JsonProperty("availableGameStats") AvailableGameStats availableGameStats
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AvailableGameStats(
            @JsonProperty("achievements") List<Achievement> achievements
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Achievement(
            @JsonProperty("name") String name,
            @JsonProperty("defaultvalue") int defaultvalue,
            @JsonProperty("displayName") String displayName,
            @JsonProperty("hidden") int hidden,
            @JsonProperty("description") String description,
            @JsonProperty("icon") String icon,
            @JsonProperty("icongray") String icongray
    ) {}
}

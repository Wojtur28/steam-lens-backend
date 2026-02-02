package com.example.steamlensbackend.achievement.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SteamTopAchievementsResponse(
        @JsonProperty("response") Response response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            @JsonProperty("games") List<Game> games
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Game(
            @JsonProperty("appid") long appid,
            @JsonProperty("name") String name,
            @JsonProperty("achievements") List<Achievement> achievements
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Achievement(
            @JsonProperty("name") String name,
            @JsonProperty("apiname") String apiname,
            @JsonProperty("icon") String icon,
            @JsonProperty("icon_gray") String iconGray,
            @JsonProperty("description") String description,
            @JsonProperty("unlocktime") long unlocktime,
            @JsonProperty("percent") float percent
    ) {}
}

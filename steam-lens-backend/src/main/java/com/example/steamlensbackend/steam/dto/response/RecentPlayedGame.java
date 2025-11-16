package com.example.steamlensbackend.steam.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecentPlayedGame(
        String name,
        @JsonProperty("img_icon_url") String imgIconUrl,
        @JsonProperty("playtime_forever_hours") Integer playtimeForeverHours,
        @JsonProperty("playtime_forever_minutes") Double playtimeForeverMinutes,
        @JsonProperty("playtime_2weeks_hours") Integer playtime2WeeksHours,
        @JsonProperty("playtime_2weeks_minutes") Double playtime2WeeksMinutes
) {
}

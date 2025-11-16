package com.example.steamlensbackend.steam.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecentPlayedGame(
        String name,
        @JsonProperty("img_icon_url") String imgIconUrl,
        @JsonProperty("playtime_forever_hours") Long playtimeForeverHours,
        @JsonProperty("playtime_forever_minutes") Long playtimeForeverMinutes,
        @JsonProperty("playtime_2weeks_hours") Long playtime2WeeksHours,
        @JsonProperty("playtime_2weeks_minutes") Long playtime2WeeksMinutes
) {
}

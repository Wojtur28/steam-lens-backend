package com.example.steamlensbackend.steam.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DashboardStatisticResponse(
        @JsonProperty("total_last_2weeks_playtime") TotalLastTwoWeeksPlaytime totalLastTwoWeeksPlaytime,
        @JsonProperty("number_of_recent_played_games") Integer numberOfRecentPlayedGames,
        @JsonProperty("recent_played_games") List<RecentPlayedGame> recentPlayedGames
) {
}

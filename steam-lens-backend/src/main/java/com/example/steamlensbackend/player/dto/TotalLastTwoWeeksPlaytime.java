package com.example.steamlensbackend.player.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TotalLastTwoWeeksPlaytime(
        @JsonProperty("total_last_2weeks_playtime_hours") Long totalLast2WeeksPlaytimeHours,
        @JsonProperty("total_last_2weeks_playtime_minutes") Long totalLast2WeeksPlaytimeMinutes
) {}

package com.example.steamlensbackend.family.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FamilyGroupDetailsResponse(
    @JsonProperty("name") String name,
    @JsonProperty("members") List<FamilyMember> members,
    @JsonProperty("free_spots") Integer freeSpots,
    @JsonProperty("country") String country,
    @JsonProperty("slot_cooldown_remaining_seconds") Long slotCooldownRemainingSeconds,
    @JsonProperty("slot_cooldown_overrides") Integer slotCooldownOverrides
) {
    public record FamilyMember(
        @JsonProperty("steamid") String steamId,
        @JsonProperty("role") Integer role,
        @JsonProperty("time_joined") Long timeJoined,
        @JsonProperty("cooldown_seconds_remaining") Long cooldownSecondsRemaining
    ) {}
}

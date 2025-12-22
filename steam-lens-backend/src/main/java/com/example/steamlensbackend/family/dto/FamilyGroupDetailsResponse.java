package com.example.steamlensbackend.family.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FamilyGroupDetailsResponse(
    @JsonProperty("family_group") FamilyGroupData familyGroup
) {
    public record FamilyGroupData(
        @JsonProperty("family_groupid") String familyGroupId,
        @JsonProperty("name") String name,
        @JsonProperty("members") List<FamilyMember> members
    ) {}

    public record FamilyMember(
        @JsonProperty("steamid") String steamId,
        @JsonProperty("role") Integer role,
        @JsonProperty("time_joined") Integer timeJoined
    ) {}
}

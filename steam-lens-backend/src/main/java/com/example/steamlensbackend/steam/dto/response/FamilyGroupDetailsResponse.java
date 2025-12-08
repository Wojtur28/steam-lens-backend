package com.example.steamlensbackend.steam.dto.response;

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
        @JsonProperty("role") Integer role, // 1 = Adult, 2 = Child
        @JsonProperty("time_joined") Integer timeJoined
    ) {}
}

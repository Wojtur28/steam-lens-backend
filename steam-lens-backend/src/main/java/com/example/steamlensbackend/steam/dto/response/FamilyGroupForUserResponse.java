package com.example.steamlensbackend.steam.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FamilyGroupForUserResponse(
    @JsonProperty("family_groupid") String familyGroupId
) {}

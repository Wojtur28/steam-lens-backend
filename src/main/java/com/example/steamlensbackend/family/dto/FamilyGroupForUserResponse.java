package com.example.steamlensbackend.family.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FamilyGroupForUserResponse(
    @JsonProperty("family_groupid") String familyGroupId
) {}

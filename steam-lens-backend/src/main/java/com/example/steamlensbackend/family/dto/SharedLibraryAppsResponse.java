package com.example.steamlensbackend.family.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SharedLibraryAppsResponse(
        @JsonProperty("apps") List<SharedApp> apps,
        @JsonProperty("owner_steamid") String ownerSteamId
) {
    public record SharedApp(
            @JsonProperty("appid") Long appId,
            @JsonProperty("owner_steamids") List<String> ownerSteamIds,
            @JsonProperty("name") String name,
            @JsonProperty("capsule_filename") String capsuleFilename,
            @JsonProperty("img_icon_hash") String imgIconHash,
            @JsonProperty("exclude_reason") Integer excludeReason,
            @JsonProperty("rt_time_acquired") Long rtTimeAcquired,
            @JsonProperty("rt_last_played") Long rtLastPlayed,
            @JsonProperty("rt_playtime") Long rtPlaytime,
            @JsonProperty("app_type") Integer appType,
            @JsonProperty("content_descriptors") List<Integer> contentDescriptors
    ) {}
}

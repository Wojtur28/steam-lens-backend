package com.example.steamlensbackend.family.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SteamWishlistResponse(
    @JsonProperty("items") List<WishlistItem> items
) {
    public record WishlistItem(
        @JsonProperty("appid") Long appId,
        @JsonProperty("priority") Integer priority,
        @JsonProperty("date_added") Long dateAdded
    ) {}
}

package com.example.steamlensbackend.game.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GameResponse(
        long appid,
        String name,
        @JsonProperty("playtime_forever") int playtimeForever,
        @JsonProperty("img_icon_url") String imgIconUrl,
        @JsonProperty("has_community_visible_stats") boolean hasCommunityVisibleStats,
        @JsonProperty("playtime_windows_forever") int playtimeWindowsForever,
        @JsonProperty("playtime_mac_forever") int playtimeMacForever,
        @JsonProperty("playtime_linux_forever") int playtimeLinuxForever,
        @JsonProperty("playtime_deck_forever") int playtimeDeckForever,
        @JsonProperty("rtime_last_played") int rtimeLastPlayed,
        @JsonProperty("capsule_filename") String capsuleFilename,
        @JsonProperty("has_workshop") boolean hasWorkshop,
        @JsonProperty("has_market") boolean hasMarket,
        @JsonProperty("has_dlc") boolean hasDlc,
        @JsonProperty("playtime_disconnected") int playtimeDisconnected,
        @JsonProperty("playtime_2weeks") int playtime2Weeks
) {
}

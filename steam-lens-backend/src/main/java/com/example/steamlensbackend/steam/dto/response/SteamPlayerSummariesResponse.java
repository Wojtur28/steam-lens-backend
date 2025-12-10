package com.example.steamlensbackend.steam.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SteamPlayerSummariesResponse(
        @JsonProperty("response")
        Response response
) {
    public record Response(
            @JsonProperty("players")
            List<Player> players
    ) {}

    public record Player(
            @JsonProperty("steamid")
            String steamid,
            @JsonProperty("communityvisibilitystate")
            Integer communityvisibilitystate,
            @JsonProperty("profilestate")
            Integer profilestate,
            @JsonProperty("personaname")
            String personaname,
            @JsonProperty("profileurl")
            String profileurl,
            @JsonProperty("avatar")
            String avatar,
            @JsonProperty("avatarmedium")
            String avatarmedium,
            @JsonProperty("avatarfull")
            String avatarfull,
            @JsonProperty("avatarhash")
            String avatarhash,
            @JsonProperty("lastlogoff")
            Long lastlogoff,
            @JsonProperty("personastate")
            Integer personastate,
            @JsonProperty("realname")
            String realname,
            @JsonProperty("primaryclanid")
            String primaryclanid,
            @JsonProperty("timecreated")
            Long timecreated,
            @JsonProperty("personastateflags")
            Integer personastateflags,
            @JsonProperty("loccountrycode")
            String loccountrycode
    ) {}
}

package com.example.steamlensbackend.player.dto;

import com.example.steamlensbackend.game.dto.GameResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OwnedGamesResponse(
        @JsonProperty("game_count") int gameCount,
        List<GameResponse> games
) {
}

package com.example.steamlensbackend.steam.services;

import com.example.steamlensbackend.steam.dto.response.GameResponse;
import com.example.steamlensbackend.steam.wrappers.SuccessResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class PlayerService {
    private SteamService steamService;

    public PlayerService(SteamService steamService) {
        this.steamService = steamService;
    }

    public Mono<SuccessResponse<List<GameResponse>>> getUserGames(String steamid) {
        return steamService.getUserOwnedGames(steamid, null).map(
                response -> SuccessResponse.of(response.response().games())
        );
    }

    public Mono<Map<String, Integer>> getNumberOfUserGames(String steamid) {
        return steamService.getUserOwnedGames(steamid, null).map(
                response -> Map.of(
                        "gamesCount", response.response().gameCount()
                )
        );
    }
}

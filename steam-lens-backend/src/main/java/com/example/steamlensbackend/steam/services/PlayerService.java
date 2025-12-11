package com.example.steamlensbackend.steam.services;

import com.example.steamlensbackend.steam.dto.response.GameResponse;
import com.example.steamlensbackend.steam.dto.response.OwnedGamesResponse;
import com.example.steamlensbackend.steam.dto.response.SteamBaseResponse;
import com.example.steamlensbackend.steam.wrappers.PagedResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PlayerService {
    private final SteamService steamService;

    public PlayerService(SteamService steamService) {
        this.steamService = steamService;
    }

    public PagedResponse<List<GameResponse>> getUserGames(String steamid, int page, int pageSize) {
        SteamBaseResponse<OwnedGamesResponse> response = steamService.getUserOwnedGames(steamid, null);
        List<GameResponse> gameResponses = response.response().games();
        return PageableService.paginate(gameResponses, page, pageSize);
    }

    public Map<String, Integer> getNumberOfUserGames(String steamid) {
        SteamBaseResponse<OwnedGamesResponse> response = steamService.getUserOwnedGames(steamid, null);
        return Map.of("gamesCount", response.response().gameCount());
    }
}

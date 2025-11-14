package com.example.steamlensbackend.steam.services;

import com.example.steamlensbackend.steam.dto.response.GameResponse;
import com.example.steamlensbackend.steam.wrappers.Meta;
import com.example.steamlensbackend.steam.wrappers.PagedResponse;
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

    public Mono<PagedResponse<List<GameResponse>>> getUserGames(String steamid, int page, int pageSize) {

        return steamService.getUserOwnedGames(steamid, null).map(
                response -> response.response().games()
        )
        .map(
            gameResponses -> {
                int totalPages = (int) Math.ceil((double) gameResponses.size() / pageSize);
                int offset = page * pageSize;

                List<GameResponse> content = gameResponses.stream()
                        .skip(offset)
                        .limit(pageSize)
                        .toList();

                return PagedResponse.of(content, new Meta(page, pageSize, totalPages, gameResponses.size()));
            }
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

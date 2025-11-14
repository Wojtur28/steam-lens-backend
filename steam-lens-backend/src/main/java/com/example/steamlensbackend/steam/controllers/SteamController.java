package com.example.steamlensbackend.steam.controllers;

import com.example.steamlensbackend.steam.dto.requests.PageableRequest;
import com.example.steamlensbackend.steam.dto.response.GameResponse;
import com.example.steamlensbackend.steam.services.PlayerService;
import com.example.steamlensbackend.steam.wrappers.PagedResponse;
import com.example.steamlensbackend.steam.wrappers.SuccessResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/steam")
public class SteamController {
    private final PlayerService playerService;

    public SteamController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/user/games/{steamId}")
    public Mono<PagedResponse<List<GameResponse>>> getUserOwnedGames(@PathVariable String steamId, PageableRequest pageableRequest) {
        return playerService.getUserGames(steamId, pageableRequest.getPage(), pageableRequest.getPageSize());
    }
}

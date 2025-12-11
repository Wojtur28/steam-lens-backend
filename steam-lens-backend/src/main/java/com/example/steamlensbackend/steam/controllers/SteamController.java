package com.example.steamlensbackend.steam.controllers;

import com.example.steamlensbackend.steam.dto.requests.PageableRequest;
import com.example.steamlensbackend.steam.dto.response.DashboardStatisticResponse;
import com.example.steamlensbackend.steam.dto.response.GameResponse;
import com.example.steamlensbackend.steam.dto.response.SteamGameDetailsResponse;
import com.example.steamlensbackend.steam.dto.response.SteamPlayerSummariesResponse;
import com.example.steamlensbackend.steam.services.PlayerService;
import com.example.steamlensbackend.steam.services.StatisticsService;
import com.example.steamlensbackend.steam.services.SteamService;
import com.example.steamlensbackend.steam.wrappers.PagedResponse;
import com.example.steamlensbackend.steam.wrappers.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/steam")
public class SteamController {
    private final PlayerService playerService;
    private final StatisticsService statisticsService;
    private final SteamService steamService;

    public SteamController(PlayerService playerService,  StatisticsService statisticsService, SteamService steamService) {
        this.playerService = playerService;
        this.statisticsService = statisticsService;
        this.steamService = steamService;
    }

    @GetMapping("/user/games/{steamId}")
    public ResponseEntity<PagedResponse<List<GameResponse>>> getUserOwnedGames(@PathVariable String steamId, PageableRequest pageableRequest) {
        return ResponseEntity.ok(playerService.getUserGames(steamId, pageableRequest.getPage(), pageableRequest.getPageSize()));
    }

    @GetMapping("/dashboard/{steamId}")
    public ResponseEntity<SuccessResponse<DashboardStatisticResponse>> getUserStatistics(@PathVariable String steamId) {
        return ResponseEntity.ok(this.statisticsService.getDashboardStatistics(steamId));
    }

    @GetMapping("/games/{appId}")
    public ResponseEntity<SuccessResponse<SteamGameDetailsResponse>> getGameDetails(@PathVariable String appId) {
        return ResponseEntity.ok(SuccessResponse.of(steamService.getSteamGameDetails(appId)));
    }

    @GetMapping("/users/{steamids}/summaries")
    public ResponseEntity<SuccessResponse<SteamPlayerSummariesResponse>> getPlayerSummaries(@PathVariable String steamids) {
        return ResponseEntity.ok(SuccessResponse.of(steamService.getPlayerSummaries(steamids)));
    }
}

package com.example.steamlensbackend.player.controller;

import com.example.steamlensbackend.common.wrappers.PagedResponse;
import com.example.steamlensbackend.common.wrappers.SuccessResponse;
import com.example.steamlensbackend.game.dto.GameResponse;
import com.example.steamlensbackend.player.dto.DashboardStatisticResponse;
import com.example.steamlensbackend.player.dto.SteamPlayerSummariesResponse;
import com.example.steamlensbackend.player.service.PlayerService;
import com.example.steamlensbackend.player.service.StatisticsService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {
    private final PlayerService playerService;
    private final StatisticsService statisticsService;

    public PlayerController(PlayerService playerService,  StatisticsService statisticsService) {
        this.playerService = playerService;
        this.statisticsService = statisticsService;
    }

    @GetMapping("/{steamId}/games")
    public ResponseEntity<PagedResponse<List<GameResponse>>> getUserOwnedGames(@RequestHeader("X-API-KEY") String apiKey, @PathVariable String steamId, Pageable pageable) {
        return ResponseEntity.ok(playerService.getUserGames(steamId, pageable, apiKey));
    }

    @GetMapping("/{steamId}/dashboard")
    public ResponseEntity<SuccessResponse<DashboardStatisticResponse>> getUserStatistics(@RequestHeader("X-API-KEY") String apiKey, @PathVariable String steamId) {
        return ResponseEntity.ok(this.statisticsService.getDashboardStatistics(steamId, apiKey));
    }

    @GetMapping("/summaries")
    public ResponseEntity<SuccessResponse<SteamPlayerSummariesResponse>> getPlayerSummaries(@RequestHeader("X-API-KEY") String apiKey, @RequestParam String steamids) {
        return ResponseEntity.ok(SuccessResponse.of(playerService.getPlayerSummaries(steamids, apiKey)));
    }
}

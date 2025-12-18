package com.example.steamlensbackend.game.controller;

import com.example.steamlensbackend.common.wrappers.SuccessResponse;
import com.example.steamlensbackend.game.dto.SteamGameDetailsResponse;
import com.example.steamlensbackend.game.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/{appId}")
    public ResponseEntity<SuccessResponse<SteamGameDetailsResponse>> getGameDetails(@PathVariable String appId) {
        return ResponseEntity.ok(SuccessResponse.of(gameService.getSteamGameDetails(appId)));
    }
}

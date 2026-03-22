package com.example.steamlensbackend.achievement.controller;

import com.example.steamlensbackend.achievement.dto.AchievementAsyncAcceptedResponse;
import com.example.steamlensbackend.achievement.dto.AchievementAsyncStatusResponse;
import com.example.steamlensbackend.achievement.dto.TopAchievementsRequest;
import com.example.steamlensbackend.achievement.service.AchievementAsyncService;
import com.example.steamlensbackend.common.wrappers.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementAsyncService achievementAsyncService;

    @PostMapping("/top")
    public ResponseEntity<SuccessResponse<AchievementAsyncAcceptedResponse>> getTopAchievements(
            @RequestHeader("X-API-KEY") String apiKey,
            @Valid @RequestBody TopAchievementsRequest request,
            HttpServletRequest httpRequest) {

        String requestId = achievementAsyncService.initiateTopAchievementsRequest(
                request.steamId(),
                apiKey,
                request.maxAchievements(),
                request.language()
        );

        String baseUrl = getBaseUrl(httpRequest);
        AchievementAsyncAcceptedResponse response = AchievementAsyncAcceptedResponse.of(requestId, baseUrl);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(SuccessResponse.of(response));
    }

    @GetMapping("/status/{requestId}")
    public ResponseEntity<SuccessResponse<AchievementAsyncStatusResponse>> getRequestStatus(
            @PathVariable String requestId) {
        AchievementAsyncStatusResponse status = achievementAsyncService.getRequestStatus(requestId);
        return ResponseEntity.ok(SuccessResponse.of(status));
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);
        return url.toString();
    }
}

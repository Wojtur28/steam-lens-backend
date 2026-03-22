package com.example.steamlensbackend.achievement.service;

import com.example.steamlensbackend.achievement.dto.TopAchievementsResponse;
import com.example.steamlensbackend.achievement.dto.steam.SteamTopAchievementsResponse;
import com.example.steamlensbackend.achievement.exception.SteamAchievementApiException;
import com.example.steamlensbackend.common.dto.SteamBaseResponse;
import com.example.steamlensbackend.player.dto.OwnedGamesResponse;
import com.example.steamlensbackend.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private static final String GET_TOP_ACHIEVEMENTS_PATH = "/IPlayerService/GetTopAchievementsForGames/v1/";

    @Qualifier("steamWebClient")
    private final RestTemplate webClient;
    private final PlayerService playerService;

    @Cacheable(value = "achievementsTop", key = "#steamId + ':' + #maxAchievements")
    public TopAchievementsResponse getTopAchievements(
            String steamId,
            String apiKey,
            int maxAchievements,
            String language
    ) {
        log.info("Fetching top achievements for steamId: {}, maxAchievements: {}", steamId, maxAchievements);

        // 1. Get owned games
        SteamBaseResponse<OwnedGamesResponse> ownedGamesResponse =
                playerService.getUserOwnedGames(steamId, null, apiKey);

        if (ownedGamesResponse == null || ownedGamesResponse.response() == null
                || ownedGamesResponse.response().games() == null) {
            log.warn("No games found for steamId: {}", steamId);
            return new TopAchievementsResponse(steamId, 0, List.of(), Instant.now());
        }

        List<Long> appIds = ownedGamesResponse.response().games().stream()
                .map(game -> game.appid())
                .limit(300) // Safety limit to avoid extremely long URLs
                .toList();

        if (appIds.isEmpty()) {
            log.warn("No app IDs found for steamId: {}", steamId);
            return new TopAchievementsResponse(steamId, 0, List.of(), Instant.now());
        }

        log.debug("Found {} games for steamId: {}", appIds.size(), steamId);

        // 2. Call Steam API: GetTopAchievementsForGames
        SteamTopAchievementsResponse steamResponse = fetchTopAchievements(
                steamId, apiKey, maxAchievements, language, appIds
        );

        // 3. Transform and enrich achievements
        List<TopAchievementsResponse.AchievementDetail> achievements =
                transformAchievements(steamResponse);

        // 4. Sort by rarity (legendary first) and limit
        List<TopAchievementsResponse.AchievementDetail> sortedAchievements = achievements.stream()
                .sorted(Comparator.comparingDouble(TopAchievementsResponse.AchievementDetail::globalPercent))
                .limit(maxAchievements)
                .toList();

        log.info("Successfully fetched {} achievements for steamId: {}", sortedAchievements.size(), steamId);

        return new TopAchievementsResponse(
                steamId,
                sortedAchievements.size(),
                sortedAchievements,
                Instant.now()
        );
    }

    private SteamTopAchievementsResponse fetchTopAchievements(
            String steamId,
            String apiKey,
            int maxAchievements,
            String language,
            List<Long> appIds
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(GET_TOP_ACHIEVEMENTS_PATH)
                .queryParam("key", apiKey)
                .queryParam("steamid", steamId)
                .queryParam("max_achievements", maxAchievements)
                .queryParam("language", language);

        // Add all appIds as array parameters
        appIds.forEach(appId -> builder.queryParam("appids[]", appId));

        String url = builder.toUriString();

        log.debug("Calling Steam API: {}", GET_TOP_ACHIEVEMENTS_PATH);

        try {
            ResponseEntity<SteamTopAchievementsResponse> response = webClient.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            SteamTopAchievementsResponse body = response.getBody();
            if (body == null || body.response() == null) {
                throw new SteamAchievementApiException("Steam API returned null response");
            }

            return body;

        } catch (HttpClientErrorException e) {
            log.error("Steam API error for top achievements: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 403) {
                throw new SteamAchievementApiException("Steam profile is not public or access denied", e);
            }

            throw new SteamAchievementApiException(
                    "Steam API returned error: " + e.getStatusCode(), e);

        } catch (Exception e) {
            log.error("Unexpected error fetching top achievements: {}", e.getMessage(), e);
            throw new SteamAchievementApiException("Error during Steam API request", e);
        }
    }

    private List<TopAchievementsResponse.AchievementDetail> transformAchievements(
            SteamTopAchievementsResponse steamResponse
    ) {
        if (steamResponse.response() == null || steamResponse.response().games() == null) {
            return List.of();
        }

        return steamResponse.response().games().stream()
                .filter(game -> game.achievements() != null)
                .flatMap(game -> game.achievements().stream()
                        .map(achievement -> new TopAchievementsResponse.AchievementDetail(
                                game.appid(),
                                game.name(),
                                achievement.name(),
                                achievement.apiname(),
                                achievement.description(),
                                achievement.icon(),
                                achievement.iconGray(),
                                Instant.ofEpochSecond(achievement.unlocktime()),
                                achievement.percent(),
                                calculateRarity(achievement.percent())
                        ))
                )
                .collect(Collectors.toList());
    }

    private String calculateRarity(float percent) {
        if (percent > 50) return "common";
        if (percent >= 25) return "uncommon";
        if (percent >= 10) return "rare";
        if (percent >= 5) return "epic";
        return "legendary";
    }
}

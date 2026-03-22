package com.example.steamlensbackend.player.service;

import com.example.steamlensbackend.common.PageableService;
import com.example.steamlensbackend.common.dto.SteamBaseResponse;
import com.example.steamlensbackend.common.exceptions.SteamException;
import com.example.steamlensbackend.common.wrappers.PagedResponse;
import com.example.steamlensbackend.game.dto.GameResponse;
import com.example.steamlensbackend.player.dto.OwnedGamesResponse;
import com.example.steamlensbackend.player.dto.SteamPlayerSummariesResponse;
import com.example.steamlensbackend.player.dto.options.GetOwnedGamesOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class PlayerService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    private static final String GET_OWNED_GAMES_PATH = "/IPlayerService/GetOwnedGames/v1/";
    private static final String GET_PLAYER_SUMMARIES_PATH = "/ISteamUser/GetPlayerSummaries/v2/";

    private final RestTemplate webClient;

    public PlayerService(@Qualifier("steamWebClient") RestTemplate webClient) {
        this.webClient = webClient;
    }

    public PagedResponse<List<GameResponse>> getUserGames(String steamid, Pageable pageable, String apiKey) {
        SteamBaseResponse<OwnedGamesResponse> response = getUserOwnedGames(steamid, null, apiKey);
        List<GameResponse> gameResponses = response.response().games();
        return PageableService.paginate(gameResponses, pageable);
    }

    @Cacheable(value = "steamGames", key = "#steamId")
    public SteamBaseResponse<OwnedGamesResponse> getUserOwnedGames(
            String steamId,
            GetOwnedGamesOptions options,
            String apiKey
    ) {
        GetOwnedGamesOptions defaultOptions = GetOwnedGamesOptions.defaultOptions();
        GetOwnedGamesOptions finalOptions = options == null ? defaultOptions : options.mergeWithDefaults(defaultOptions);

        logger.debug("Fetching owned games from Steam API for steamId: {}", steamId);

        String url = UriComponentsBuilder.fromPath(GET_OWNED_GAMES_PATH)
                .queryParam("key", apiKey)
                .queryParam("steamid", steamId)
                .queryParam("include_appinfo", finalOptions.includeAppInfo())
                .queryParam("include_played_free_games", finalOptions.includePlayedFreeGames())
                .queryParam("language", finalOptions.language())
                .queryParam("include_free_sub", finalOptions.includeFreeSub())
                .queryParam("skip_unvetted_apps", finalOptions.skipUnvettedApps())
                .queryParam("include_extended_appinfo", finalOptions.includeExtendedAppInfo())
                .toUriString();

        return executeRequest(
                webClient,
                url,
                new ParameterizedTypeReference<>() {},
                "owned games for steamId: " + steamId
        );
    }

    @Cacheable(value = "playerSummaries", key = "#steamids")
    public SteamPlayerSummariesResponse getPlayerSummaries(String steamids, String apiKey) {
        String url = UriComponentsBuilder.fromPath(GET_PLAYER_SUMMARIES_PATH)
                .queryParam("key", apiKey)
                .queryParam("steamids", steamids)
                .toUriString();

        return executeRequest(
                webClient,
                url,
                new ParameterizedTypeReference<>() {},
                "player summaries"
        );
    }

    private <T> T executeRequest(RestTemplate restTemplate, String url, ParameterizedTypeReference<T> responseType, String errorContext) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    responseType
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Steam API error for {}: {} - {}", errorContext, e.getStatusCode(), e.getResponseBodyAsString());
            throw new SteamException("Steam API returned error: " + e.getStatusCode(), e, HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (Exception e) {
            logger.error("An unexpected error occurred for {}: {}", errorContext, e.getMessage(), e);
            throw new SteamException("Error during Steam API request for " + errorContext, e);
        }
    }
}

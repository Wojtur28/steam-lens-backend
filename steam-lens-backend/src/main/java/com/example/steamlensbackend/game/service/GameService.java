package com.example.steamlensbackend.game.service;

import com.example.steamlensbackend.common.exceptions.SteamException;
import com.example.steamlensbackend.game.dto.SteamAppWrapper;
import com.example.steamlensbackend.game.dto.SteamGameDetailsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private static final String APP_DETAILS_PATH = "/api/appdetails";

    private final RestTemplate storeWebClient;

    public GameService(@Qualifier("steamStoreWebClient") RestTemplate storeWebClient) {
        this.storeWebClient = storeWebClient;
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
            throw new SteamException("Steam API returned error: " + e.getStatusCode(), e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred for {}: {}", errorContext, e.getMessage(), e);
            throw new SteamException("Error during Steam API request for " + errorContext, e);
        }
    }

    @Cacheable(value = "gameDetails", key = "#appId")
    public SteamGameDetailsResponse getSteamGameDetails(String appId) {
        String url = UriComponentsBuilder.fromPath(APP_DETAILS_PATH)
                .queryParam("appids", appId)
                .toUriString();

        Map<String, SteamAppWrapper> responseMap = executeRequest(
                storeWebClient,
                url,
                new ParameterizedTypeReference<>() {},
                "game details for appId: " + appId
        );

        return Optional.ofNullable(responseMap)
                .map(map -> map.get(appId))
                .filter(SteamAppWrapper::success)
                .map(SteamAppWrapper::data)
                .orElseThrow(() -> new SteamException("Game details not found or success is false for appId: " + appId, null));
    }
}

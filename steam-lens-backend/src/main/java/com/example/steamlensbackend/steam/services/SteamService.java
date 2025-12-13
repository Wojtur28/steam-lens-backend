package com.example.steamlensbackend.steam.services;

import com.example.steamlensbackend.steam.dto.options.GetOwnedGamesOptions;
import com.example.steamlensbackend.steam.dto.response.*;
import com.example.steamlensbackend.steam.dto.wrapper.SteamAppWrapper;
import com.example.steamlensbackend.steam.exceptions.SteamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SteamService {

    private static final Logger logger = LoggerFactory.getLogger(SteamService.class);

    private static final String GET_OWNED_GAMES_PATH = "/IPlayerService/GetOwnedGames/v1/";
    private static final String GET_FAMILY_GROUP_FOR_USER_PATH = "/IFamilyGroupsService/GetFamilyGroupForUser/v1/";
    private static final String GET_FAMILY_GROUP_DETAILS_PATH = "/IFamilyGroupsService/GetFamilyGroup/v1/";
    private static final String APP_DETAILS_PATH = "/api/appdetails";
    private static final String GET_SHARED_LIBRARY_APPS_PATH = "/IFamilyGroupsService/GetSharedLibraryApps/v1/";
    private static final String GET_PLAYER_SUMMARIES_PATH = "/ISteamUser/GetPlayerSummaries/v2/";

    private final RestTemplate webClient;
    private final RestTemplate storeWebClient;
    private final String apiKey;

    public SteamService(@Qualifier("steamWebClient") RestTemplate webClient,
                        @Qualifier("steamStoreWebClient") RestTemplate storeWebClient,
                        @Value("${steam.api.key}") String apiKey) {
        this.webClient = webClient;
        this.storeWebClient = storeWebClient;
        this.apiKey = apiKey;
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

    @Cacheable(value = "steamGames", key = "#steamId")
    public SteamBaseResponse<OwnedGamesResponse> getUserOwnedGames(
            String steamId,
            GetOwnedGamesOptions options
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

    public SteamBaseResponse<FamilyGroupForUserResponse> getFamilyGroupForUser(String steamId, String accessToken) {
        String url = UriComponentsBuilder.fromPath(GET_FAMILY_GROUP_FOR_USER_PATH)
                .queryParam("access_token", accessToken)
                .queryParam("include_family_group_response", true)
                .toUriString();

        return executeRequest(
                webClient,
                url,
                new ParameterizedTypeReference<>() {},
                "family group for user: " + steamId
        );
    }

    public SteamBaseResponse<FamilyGroupDetailsResponse> getFamilyGroupDetails(String familyGroupId, String accessToken) {
        String url = UriComponentsBuilder.fromPath(GET_FAMILY_GROUP_DETAILS_PATH)
                .queryParam("access_token", accessToken)
                .queryParam("family_groupid", familyGroupId)
                .toUriString();

        return executeRequest(
                webClient,
                url,
                new ParameterizedTypeReference<>() {},
                "family group details for familyGroupId: " + familyGroupId
        );
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

    public SharedLibraryWithOwnersResponse getSharedLibraryApps(String accessToken, String familyGroupId, String steamId) {
        String libraryUrl = UriComponentsBuilder.fromPath(GET_SHARED_LIBRARY_APPS_PATH)
                .queryParam("access_token", accessToken)
                .queryParam("family_groupid", familyGroupId)
                .queryParam("steamid", steamId)
                .queryParam("include_own", true)
                .queryParam("include_excluded", true)
                .queryParam("include_free", false)
                .toUriString();

        SteamBaseResponse<SharedLibraryAppsResponse> libraryBaseResponse = executeRequest(
                webClient,
                libraryUrl,
                new ParameterizedTypeReference<>() {},
                "shared library for familyGroupId: " + familyGroupId
        );

        SharedLibraryAppsResponse libraryResponse = Optional.ofNullable(libraryBaseResponse)
                .map(SteamBaseResponse::response)
                .orElseThrow(() -> new SteamException("Failed to retrieve shared library apps", null));

        Set<String> uniqueOwnerIds = libraryResponse.apps().stream()
                .flatMap(app -> app.ownerSteamIds().stream())
                .collect(Collectors.toSet());

        if (uniqueOwnerIds.isEmpty()) {
            return new SharedLibraryWithOwnersResponse(libraryResponse, Collections.emptyList());
        }

        String steamIdsParam = String.join(",", uniqueOwnerIds);
        SteamPlayerSummariesResponse summaryResponse = getPlayerSummaries(steamIdsParam);
        return new SharedLibraryWithOwnersResponse(libraryResponse, summaryResponse.response().players());
    }

    @Cacheable(value = "playerSummaries", key = "#steamids")
    public SteamPlayerSummariesResponse getPlayerSummaries(String steamids) {
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
}

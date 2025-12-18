package com.example.steamlensbackend.family.service;

import com.example.steamlensbackend.common.PageableService;
import com.example.steamlensbackend.common.dto.SteamBaseResponse;
import com.example.steamlensbackend.common.exceptions.SteamException;
import com.example.steamlensbackend.common.wrappers.PagedResponse;
import com.example.steamlensbackend.family.dto.FamilyGroupDetailsResponse;
import com.example.steamlensbackend.family.dto.FamilyGroupForUserResponse;
import com.example.steamlensbackend.family.dto.OwnerGameValue;
import com.example.steamlensbackend.family.dto.SharedLibraryAppsResponse;
import com.example.steamlensbackend.family.dto.SharedLibraryPriceResponse;
import com.example.steamlensbackend.game.dto.GamePriceInfo;
import com.example.steamlensbackend.game.dto.SteamGameDetailsResponse;
import com.example.steamlensbackend.game.service.GameService;
import com.example.steamlensbackend.player.dto.SteamPlayerSummariesResponse;
import com.example.steamlensbackend.player.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FamilyService {
    private static final Logger logger = LoggerFactory.getLogger(FamilyService.class);

    private static final String GET_FAMILY_GROUP_FOR_USER_PATH = "/IFamilyGroupsService/GetFamilyGroupForUser/v1/";
    private static final String GET_FAMILY_GROUP_DETAILS_PATH = "/IFamilyGroupsService/GetFamilyGroup/v1/";
    private static final String GET_SHARED_LIBRARY_APPS_PATH = "/IFamilyGroupsService/GetSharedLibraryApps/v1/";

    private final RestTemplate webClient;
    private final GameService gameService;
    private final PlayerService playerService;

    public FamilyService(@Qualifier("steamWebClient") RestTemplate webClient, GameService gameService, PlayerService playerService) {
        this.webClient = webClient;
        this.gameService = gameService;
        this.playerService = playerService;
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

    public SharedLibraryPriceResponse getSharedLibraryApps(String accessToken, String familyGroupId, String steamId, Pageable pageable, String apiKey) {
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

        List<GamePriceInfo> gamePriceInfos = libraryResponse.apps().parallelStream()
                .map(app -> {
                    try {
                        SteamGameDetailsResponse details = gameService.getSteamGameDetails(String.valueOf(app.appId()));
                        BigDecimal price = (details.isFree() || details.priceOverview() == null) ? BigDecimal.ZERO : BigDecimal.valueOf(details.priceOverview().finalPrice()).divide(BigDecimal.valueOf(100));
                        return new GamePriceInfo(app.appId(), details.name(), details.headerImage(), price, app.ownerSteamIds());
                    } catch (Exception e) {
                        logger.error("Failed to fetch details for app {}: {}", app.appId(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Set<String> uniqueOwnerIds = gamePriceInfos.stream()
                .flatMap(game -> game.ownerSteamIds().stream())
                .collect(Collectors.toSet());

        Map<String, SteamPlayerSummariesResponse.Player> playerSummaries = playerService.getPlayerSummaries(String.join(",", uniqueOwnerIds), apiKey)
                .response().players().stream()
                .collect(Collectors.toMap(SteamPlayerSummariesResponse.Player::steamid, Function.identity()));

        Map<String, BigDecimal> ownerTotalValues = new HashMap<>();
        Map<String, Integer> ownerGameCounts = new HashMap<>();

        for (GamePriceInfo game : gamePriceInfos) {
            for (String ownerId : game.ownerSteamIds()) {
                ownerTotalValues.merge(ownerId, game.price(), BigDecimal::add);
                ownerGameCounts.merge(ownerId, 1, Integer::sum);
            }
        }

        List<OwnerGameValue> ownerGameValues = playerSummaries.values().stream()
                .map(player -> new OwnerGameValue(
                        player.steamid(),
                        player.personaname(),
                        player.avatar(),
                        ownerTotalValues.getOrDefault(player.steamid(), BigDecimal.ZERO),
                        ownerGameCounts.getOrDefault(player.steamid(), 0)
                ))
                .toList();

        PagedResponse<List<GamePriceInfo>> pagedGames = PageableService.paginate(gamePriceInfos, pageable);
        BigDecimal totalValue = ownerGameValues.stream().map(OwnerGameValue::totalValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SharedLibraryPriceResponse(pagedGames, ownerGameValues, totalValue, libraryResponse.apps().size());
    }
}

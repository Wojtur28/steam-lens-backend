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
import com.example.steamlensbackend.game.model.SteamGameDocument;
import com.example.steamlensbackend.game.repository.SteamGameRepository;
import com.example.steamlensbackend.player.dto.SteamPlayerSummariesResponse;
import com.example.steamlensbackend.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
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

import javax.money.MonetaryAmount;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyService {
    private static final Logger logger = LoggerFactory.getLogger(FamilyService.class);

    private static final String GET_FAMILY_GROUP_FOR_USER_PATH = "/IFamilyGroupsService/GetFamilyGroupForUser/v1/";
    private static final String GET_FAMILY_GROUP_DETAILS_PATH = "/IFamilyGroupsService/GetFamilyGroup/v1/";
    private static final String GET_SHARED_LIBRARY_APPS_PATH = "/IFamilyGroupsService/GetSharedLibraryApps/v1/";

    private final SteamGameRepository steamGameRepository;
    @Qualifier("steamWebClient")
    private final RestTemplate webClient;
    private final PlayerService playerService;

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

        List<SharedLibraryAppsResponse.SharedApp> apps = libraryResponse.apps();

        List<Long> allAppIds = apps.stream().map(SharedLibraryAppsResponse.SharedApp::appId).toList();
        List<SteamGameDocument> foundGames = steamGameRepository.findAllById(allAppIds);

        Map<Long, SteamGameDocument> gamesMap = foundGames.stream()
                .collect(Collectors.toMap(SteamGameDocument::getAppId, Function.identity()));

        List<GamePriceInfo> gamePriceInfos = apps.stream()
                .map(app -> {
                    SteamGameDocument dbGame = gamesMap.get(app.appId());

                    String name = (dbGame != null) ? dbGame.getName() : app.name();
                    String image = (dbGame != null) ? dbGame.getHeaderImage() : app.capsuleFilename();

                    MonetaryAmount price = (dbGame != null) && dbGame.getPrice() != null
                            ? dbGame.getPrice()
                            : Money.of(0, "USD");

                    return new GamePriceInfo(app.appId(), name, image, price, app.ownerSteamIds());
                })
                .collect(Collectors.toList());

        Set<String> uniqueOwnerIds = gamePriceInfos.stream()
                .flatMap(game -> game.ownerSteamIds().stream())
                .collect(Collectors.toSet());

        final Map<String, SteamPlayerSummariesResponse.Player> finalPlayerSummaries = playerService.getPlayerSummaries(String.join(",", uniqueOwnerIds), apiKey)
                .response().players().stream()
                .collect(Collectors.toMap(SteamPlayerSummariesResponse.Player::steamid, Function.identity()));


        Map<String, MonetaryAmount> ownerTotalValues = new HashMap<>();
        Map<String, Integer> ownerGameCounts = new HashMap<>();

        for (GamePriceInfo game : gamePriceInfos) {
            for (String ownerId : game.ownerSteamIds()) {
                ownerTotalValues.merge(ownerId, game.price(), MonetaryAmount::add);
                ownerGameCounts.merge(ownerId, 1, Integer::sum);
            }
        }

        List<OwnerGameValue> ownerGameValues = uniqueOwnerIds.stream()
                .map(ownerId -> {
                    SteamPlayerSummariesResponse.Player player = finalPlayerSummaries.get(ownerId);
                    String name = (player != null) ? player.personaname() : "Unknown User";
                    String avatar = (player != null) ? player.avatar() : "";

                    return new OwnerGameValue(
                            ownerId,
                            name,
                            avatar,
                            ownerTotalValues.getOrDefault(ownerId, Money.of(0, "USD")),
                            ownerGameCounts.getOrDefault(ownerId, 0)
                    );
                })
                .toList();

        PagedResponse<List<GamePriceInfo>> pagedGames = PageableService.paginate(gamePriceInfos, pageable);

        MonetaryAmount totalLibraryValue = gamePriceInfos.stream()
                .map(GamePriceInfo::price)
                .reduce(Money.of(0, "USD"), MonetaryAmount::add);

        return new SharedLibraryPriceResponse(pagedGames, ownerGameValues, totalLibraryValue, libraryResponse.apps().size());
    }
}

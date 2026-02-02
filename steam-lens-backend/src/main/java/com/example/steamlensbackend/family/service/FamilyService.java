package com.example.steamlensbackend.family.service;

import com.example.steamlensbackend.common.PageableService;
import com.example.steamlensbackend.common.dto.SteamBaseResponse;
import com.example.steamlensbackend.common.exceptions.SteamException;
import com.example.steamlensbackend.common.wrappers.PagedResponse;
import com.example.steamlensbackend.family.dto.FamilyGroupDetailsResponse;
import com.example.steamlensbackend.family.dto.FamilyGroupForUserResponse;
import com.example.steamlensbackend.family.dto.FamilyWishlistResponse;
import com.example.steamlensbackend.family.dto.OwnerGameValue;
import com.example.steamlensbackend.family.dto.SharedLibraryAppsResponse;
import com.example.steamlensbackend.family.dto.SharedLibraryPriceResponse;
import com.example.steamlensbackend.family.dto.SteamWishlistResponse;
import com.example.steamlensbackend.game.dto.GamePriceInfo;
import com.example.steamlensbackend.game.model.SteamGameDocument;
import com.example.steamlensbackend.game.repository.SteamGameRepository;
import com.example.steamlensbackend.player.dto.SteamPlayerSummariesResponse;
import com.example.steamlensbackend.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
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
    private static final String GET_WISHLIST_PATH = "/IWishlistService/GetWishlist/v1/";

    private final SteamGameRepository steamGameRepository;
    @Qualifier("steamWebClient")
    private final RestTemplate steamWebClient;
    private final PlayerService playerService;
    private final WishlistCacheService wishlistCacheService;

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

    public SteamBaseResponse<FamilyGroupForUserResponse> getFamilyGroupForUser(String steamId, String accessToken) {
        String url = UriComponentsBuilder.fromPath(GET_FAMILY_GROUP_FOR_USER_PATH)
                .queryParam("access_token", accessToken)
                .queryParam("include_family_group_response", true)
                .toUriString();

        return executeRequest(
                steamWebClient,
                url,
                new ParameterizedTypeReference<>() {},
                "family group for user: " + steamId
        );
    }

    public SteamBaseResponse<FamilyGroupDetailsResponse> getFamilyGroupDetails(String accessToken, String familyGroupId, String steamId) {
        String url = UriComponentsBuilder.fromPath(GET_FAMILY_GROUP_DETAILS_PATH)
                .queryParam("access_token", accessToken)
                .queryParam("family_groupid", familyGroupId)
                .queryParam("steamid", steamId)
                .toUriString();

        try {
            ResponseEntity<String> rawResponse = steamWebClient.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
            );

            SteamBaseResponse<FamilyGroupDetailsResponse> response = executeRequest(
                    steamWebClient,
                    url,
                    new ParameterizedTypeReference<>() {},
                    "family group details for familyGroupId: " + familyGroupId
            );

            return response;
        } catch (Exception e) {
            logger.error("Error during request", e);
            throw e;
        }
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
                steamWebClient,
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

                    BigDecimal price;
                    if (dbGame != null && dbGame.getPrice() != null) {
                        price = dbGame.getPrice();
                    } else {
                        price = BigDecimal.ZERO;
                    }

                    return new GamePriceInfo(app.appId(), name, image, price, app.ownerSteamIds());
                })
                .collect(Collectors.toList());

        Set<String> uniqueOwnerIds = gamePriceInfos.stream()
                .flatMap(game -> game.ownerSteamIds().stream())
                .collect(Collectors.toSet());

        final Map<String, SteamPlayerSummariesResponse.Player> finalPlayerSummaries = playerService.getPlayerSummaries(String.join(",", uniqueOwnerIds), apiKey)
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

        List<OwnerGameValue> ownerGameValues = uniqueOwnerIds.stream()
                .map(ownerId -> {
                    SteamPlayerSummariesResponse.Player player = finalPlayerSummaries.get(ownerId);
                    String name = (player != null) ? player.personaname() : "Unknown User";
                    String avatar = (player != null) ? player.avatar() : "";

                    return new OwnerGameValue(
                            ownerId,
                            name,
                            avatar,
                            ownerTotalValues.getOrDefault(ownerId, BigDecimal.ZERO),
                            ownerGameCounts.getOrDefault(ownerId, 0)
                    );
                })
                .toList();

        PagedResponse<List<GamePriceInfo>> pagedGames = PageableService.paginate(gamePriceInfos, pageable);

        BigDecimal totalLibraryValue = gamePriceInfos.stream()
                .map(GamePriceInfo::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SharedLibraryPriceResponse(pagedGames, ownerGameValues, totalLibraryValue, libraryResponse.apps().size());
    }

    public SteamBaseResponse<SteamWishlistResponse> getWishlistForUser(String steamId, String accessToken) {
        String url = UriComponentsBuilder.fromPath(GET_WISHLIST_PATH)
                .queryParam("key", accessToken)
                .queryParam("steamid", steamId)
                .toUriString();

        return executeRequest(
                steamWebClient,
                url,
                new ParameterizedTypeReference<>() {},
                "wishlist for user: " + steamId
        );
    }

    public FamilyWishlistResponse getFamilyWishlist(String accessToken, String familyGroupId, String steamId, String apiKey, Pageable pageable) {
        Optional<WishlistCacheService.WishlistCacheEntry> cached = wishlistCacheService.getCachedWishlist(familyGroupId);

        if (cached.isPresent()) {
            WishlistCacheService.WishlistCacheEntry cacheEntry = cached.get();
            PagedResponse<List<FamilyWishlistResponse.WishlistedGame>> pagedGames =
                    PageableService.paginate(cacheEntry.games(), pageable);
            return new FamilyWishlistResponse(pagedGames, cacheEntry.games().size(), cacheEntry.totalMembers());
        }

        SteamBaseResponse<FamilyGroupDetailsResponse> familyDetails = getFamilyGroupDetails(accessToken, familyGroupId, steamId);

        List<FamilyGroupDetailsResponse.FamilyMember> members = Optional.ofNullable(familyDetails)
                .map(SteamBaseResponse::response)
                .map(FamilyGroupDetailsResponse::members)
                .orElse(Collections.emptyList());

        if (members.isEmpty()) {
            PagedResponse<List<FamilyWishlistResponse.WishlistedGame>> emptyPage =
                    PageableService.paginate(Collections.emptyList(), pageable);
            return new FamilyWishlistResponse(emptyPage, 0, 0);
        }

        List<String> memberSteamIds = members.stream()
                .map(FamilyGroupDetailsResponse.FamilyMember::steamId)
                .toList();

        Map<String, SteamPlayerSummariesResponse.Player> playerSummaries = playerService
                .getPlayerSummaries(String.join(",", memberSteamIds), apiKey)
                .response().players().stream()
                .collect(Collectors.toMap(SteamPlayerSummariesResponse.Player::steamid, Function.identity()));

        Map<Long, List<String>> gameToMembers = new HashMap<>();

        for (String memberSteamId : memberSteamIds) {
            try {
                SteamBaseResponse<SteamWishlistResponse> wishlistResponse = getWishlistForUser(memberSteamId, accessToken);

                List<SteamWishlistResponse.WishlistItem> items = Optional.ofNullable(wishlistResponse)
                        .map(SteamBaseResponse::response)
                        .map(SteamWishlistResponse::items)
                        .orElse(Collections.emptyList());

                for (SteamWishlistResponse.WishlistItem item : items) {
                    gameToMembers.computeIfAbsent(item.appId(), k -> new ArrayList<>()).add(memberSteamId);
                }
            } catch (Exception e) {
                logger.warn("Failed to fetch wishlist for user {}: {}", memberSteamId, e.getMessage());
            }
        }

        List<Long> allAppIds = new ArrayList<>(gameToMembers.keySet());
        List<SteamGameDocument> foundGames = steamGameRepository.findAllById(allAppIds);
        Map<Long, SteamGameDocument> gamesMap = foundGames.stream()
                .collect(Collectors.toMap(SteamGameDocument::getAppId, Function.identity()));

        List<FamilyWishlistResponse.WishlistedGame> wishlistedGames = gameToMembers.entrySet().stream()
                .map(entry -> {
                    Long appId = entry.getKey();
                    List<String> memberIds = entry.getValue();

                    SteamGameDocument dbGame = gamesMap.get(appId);
                    String name = (dbGame != null) ? dbGame.getName() : "Unknown Game";
                    String headerImage = (dbGame != null) ? dbGame.getHeaderImage() : "";

                    List<FamilyWishlistResponse.WishlistMember> wishlistMembers = memberIds.stream()
                            .map(memberId -> {
                                SteamPlayerSummariesResponse.Player player = playerSummaries.get(memberId);
                                String personaName = (player != null) ? player.personaname() : "Unknown User";
                                String avatar = (player != null) ? player.avatar() : "";
                                return new FamilyWishlistResponse.WishlistMember(memberId, personaName, avatar);
                            })
                            .toList();

                    return new FamilyWishlistResponse.WishlistedGame(appId, name, headerImage, wishlistMembers, wishlistMembers.size());
                })
                .sorted((a, b) -> Integer.compare(b.memberCount(), a.memberCount()))
                .toList();

        wishlistCacheService.cacheWishlist(familyGroupId, wishlistedGames, memberSteamIds.size());

        PagedResponse<List<FamilyWishlistResponse.WishlistedGame>> pagedGames =
                PageableService.paginate(wishlistedGames, pageable);

        return new FamilyWishlistResponse(pagedGames, wishlistedGames.size(), memberSteamIds.size());
    }
}
